package com.thred.datingapp.user.service;

import static com.thred.datingapp.user.properties.RedisProperties.EDIT_PROFILE_KEY;
import static com.thred.datingapp.user.properties.RedisProperties.EDIT_QUESTION_KEY;

import com.thred.datingapp.admin.repository.ReviewRepository;
import com.thred.datingapp.chat.dto.NotificationDto;
import com.thred.datingapp.chat.repository.FcmTokenRepository;
import com.thred.datingapp.common.service.NotificationService;
import com.thred.datingapp.common.type.NotificationType;
import com.thred.datingapp.common.entity.admin.Review;
import com.thred.datingapp.common.entity.admin.ReviewStatus;
import com.thred.datingapp.common.entity.admin.ReviewType;
import com.thred.datingapp.common.entity.chat.FcmToken;
import com.thred.datingapp.common.entity.user.*;
import com.thred.datingapp.common.entity.user.UserDetail;
import com.thred.datingapp.common.entity.user.field.Role;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.common.utils.RedisUtils;
import com.thred.datingapp.common.utils.S3Utils;
import com.thred.datingapp.inApp.Service.PurchaseService;
import com.thred.datingapp.inApp.repository.UserAssetRepository;
import com.thred.datingapp.user.api.request.BlockInfoRequest;
import com.thred.datingapp.user.api.request.JoinDetailsRequest;
import com.thred.datingapp.user.api.request.JoinUserRequest;
import com.thred.datingapp.user.api.request.RejoinUserRequest;
import com.thred.datingapp.user.api.response.BlockNumberResponse;
import com.thred.datingapp.user.api.response.BlockNumbersResponse;
import com.thred.datingapp.user.api.response.JoinTotalDetails;
import com.thred.datingapp.user.api.response.ProfileResponse;
import com.thred.datingapp.user.repository.*;
import com.thred.datingapp.main.dto.request.EditDetailsRequest;
import com.thred.datingapp.main.dto.request.EditProfileRequest;
import com.thred.datingapp.main.dto.request.EditTotalRequest;
import com.thred.datingapp.main.dto.request.EditUserRequest;
import com.thred.datingapp.main.dto.response.UserDetailsResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * user와 details를 통합적으로 관리(1대1 관계라서 detailsService를 따로 만들지 않았습니다!)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
  private static final String USER_REGISTRATION_REQUEST_MESSAGE = "회원가입 요청이 접수되었습니다. 요청자: %s, 이메일: %s.";
  private static final String USER_UPDATE_EDIT_REQUEST_MESSAGE  = "회원 정보 수정 요청이 접수되었습니다. 요청자: %s, 이메일: %s.";

  private final NotificationService          notificationService;
  private final ReviewRepository             reviewRepository;
  private final RandomStringGeneratorService random;
  private final UserDetailRepository         userDetailRepository;
  private final QuestionRepository           questionRepository;
  private final BlockRepository              blockRepository;
  private final S3Utils                      s3Utils;
  private final RedisUtils                   redisUtils;
  private final PictureRepository            pictureRepository;
  private final FcmTokenRepository           fcmTokenRepository;
  private final UserAssetRepository          userAssetRepository;
  private final PurchaseService              purchaseService;
  private final UserRepository               userRepository;

  public boolean checkDuplicateEmail(String email) {
    return userRepository.findByEmail(email)
                         .isPresent();
  }

  public boolean checkDuplicateName(String username) {
    return userRepository.existsByUsername(username);
  }

  public boolean checkCode(String code) {
    return userRepository.existsByCode(code);
  }

  @Transactional
  public Long join(JoinUserRequest joinUserRequest, JoinDetailsRequest details, MultipartFile mainProfile, List<MultipartFile> files) {
    boolean isExistUser = userRepository.existsByEmail(joinUserRequest.email());

    if (isExistUser) {
      log.error("[join] 이미 회원가입된 이메일입니다. ===> email = {}", joinUserRequest.email());
      throw new CustomException(UserErrorCode.ALREADY_REGISTERED_USER);
    }
    User newUser = JoinUserRequest.fromEntity(joinUserRequest);
    // 1. S3 저장 (main profile)
    String decodedMainProfileUrl = s3Utils.saveImage(mainProfile);
    newUser.updateMainProfile(decodedMainProfileUrl);
    // 2. User, UserDetail 저장
    UserDetail newUserDetail = JoinDetailsRequest.fromEntity(details, newUser);
    Long userId = completeFirstJoin(newUser, newUserDetail);
    // 3. Question 저장
    Question question = JoinUserRequest.fromEntity(joinUserRequest, newUser);
    questionRepository.save(question);
    // 4. 회원 프로필 이미지 저장
    saveMultipartFileAndPictures(files, newUser);
    // 5. Judgment 저장
    makeJudgment(newUser, ReviewType.JOIN);
    // 6. FCM 정보 저장
    FcmToken fcmToken = FcmToken.builder()
                                .token(joinUserRequest.fcmToken())
                                .member(newUser)
                                .build();
    fcmTokenRepository.save(fcmToken);
    // 7. FCM 알림 전송
    sendNotificationToAdmin(String.format(USER_REGISTRATION_REQUEST_MESSAGE, newUser.getUsername(), newUser.getEmail()));
    log.info("[join] 회원가입 요청 완료 ===> email: {}", joinUserRequest.email());
    return userId;
  }

  @Transactional
  public void rejoin(RejoinUserRequest rejoinUserRequest, JoinDetailsRequest details, boolean mainChange, MultipartFile mainProfile,
                     List<Long> deleteFileIds, List<MultipartFile> newProfiles) {

    // 1. 기존 회원 조회
    User existingUser = getExistingUser(rejoinUserRequest);
    // 2. Main 프로필 업데이트
    User newUser = createNewUserFromRequest(rejoinUserRequest, existingUser, mainChange, mainProfile);
    // 3. 회원 detail 업데이트
    UserDetail newUserDetail = JoinDetailsRequest.fromEntity(details, newUser);
    updateExistingUserDetail(existingUser, newUserDetail);
    // 4. 기존 프로필 삭제 및 새 프로필 업데이트
    deleteOldPictures(deleteFileIds);
    saveNewProfilesIfNeeded(newProfiles, existingUser);
    // 5. 기존 질문 삭제 및 새 질문 저장
    questionRepository.deleteById(existingUser.getId());
    Question question = RejoinUserRequest.fromEntity(rejoinUserRequest, existingUser);
    questionRepository.save(question);
    // 6. Judgment 생성
    makeJudgment(existingUser, ReviewType.JOIN);
    log.info("[rejoin] 재가입 요청 완료 ===> email: {}", rejoinUserRequest.email());
    // 7. FCM 알림 전송
    sendNotificationToAdmin(USER_REGISTRATION_REQUEST_MESSAGE);
  }

  // 추천인 코드
  @Transactional
  public void joinCodeEvent(Long referredUserId, String code) {
    User referrerUser = userRepository.findByCode(code)
                                      .orElseThrow(() -> {
                                        log.error("[joinCodeEvent] 존재하지 않은 코드입니다.(Not exist user) ===> referralCode: {}", code);
                                        return new CustomException(UserErrorCode.INVALID_CODE);
                                      });
    User referredUser = getUserById(referredUserId);
    purchaseService.updateThreadQuantityByReferralCode(referredUser, referrerUser);
  }

  @Transactional
  public void changeJoinStatus(Long userId, boolean result) {
    User user = getUserById(userId);
    if (result) {
      user.successJoin();
    } else {
      user.failJoin();
    }
    log.info("[changeJoinResult] 회원 가입 결과 업데이트 완료 ===> userId: {}", userId);
  }

  @Transactional
  public void setBlockNumber(Long userId, List<BlockInfoRequest> blockInfoRequests) {
    // 1. 차단 요청을 보낸 회원 조회 (차단자)
    User blocker = getUserById(userId);
    // 2. 차단 대상 회원 목록 조회 (이름 + 전화번호로 매칭)
    List<User> blockedUsers = getBlockedUsers(blockInfoRequests);
    // 3. 해당 사용자가 이전에 차단했던 모든 기록 삭제 (차단 목록 초기화)
    blockRepository.deleteByBlockerId(userId);
    // 4. 차단 요청 목록이 비어 있을 경우, 차단 처리 없이 종료
    if (blockInfoRequests.isEmpty()) {
      log.info("[setBlockNumber] 차단된 연락처 없음 ===> userId: {}", userId);
      return;
    }
    // 5. 새로운 차단 대상이 있을 경우, 차단 정보 저장
    for (User blockedUser : blockedUsers) {
      Block newBlock = Block.builder()
                            .blockedUser(blockedUser)
                            .blocker(blocker)
                            .build();
      blockRepository.save(newBlock);
    }
    log.info("[setBlockNumber] 연락처 차단 정보 수정 완료 ===> userId: {}", userId);
  }

  public BlockNumbersResponse getBlockNumber(Long userId) {
    List<Block> blocks = blockRepository.findByBlockerId(userId);
    List<BlockNumberResponse> blockResponses = blocks.stream()
                                                     .map(block -> new BlockNumberResponse(block.getBlockedUser().getUsername(),
                                                                                           block.getBlockedUser().getPhoneNumber()))
                                                     .toList();
    log.info("[getBlockNumber] 연락처 차단 조회 성공 ===> userId: {}", userId);
    return new BlockNumbersResponse(blockResponses);
  }

  @Transactional
  public void changePhoneNumber(Long id, String number) {
    User user = getUserById(id);
    user.changePhoneNumber(number);
    log.info("[changePhoneNumber] 핸드폰 번호 수정 완료 ===> userId: {}", id);
  }

  @Transactional
  public void changeAddress(Long id, String city, String province) {
    User user = getUserById(id);
    user.changeAddress(city, province);
    log.info("[changeAddress] 주소 정보 수정 완료 ===> userId: {}", id);
  }

  @Transactional
  public void sendEditProfilesRequest(Long id, boolean mainChange, MultipartFile mainProfile, List<Long> changedProfileIds,
                                      List<MultipartFile> changedExtraProfiles) {
    Map<String, String> profilesInfo = new HashMap<>();
    if (changedExtraProfiles != null) {
      changedExtraProfiles.forEach(picture -> {
        String s3url = s3Utils.saveImage(picture);
        profilesInfo.put(picture.getOriginalFilename(), s3url);
      });
    }

    String main = null;
    if (mainChange) {
      main = s3Utils.saveImage(mainProfile);
    }

    List<Picture> existImages;
    if (changedProfileIds != null && !changedProfileIds.isEmpty()) {
      existImages = pictureRepository.findAllByUserIdAndIdNotIn(id, changedProfileIds);
    } else {
      existImages = pictureRepository.findAllByUserId(id);
    }

    List<String> existImagePaths = existImages.stream()
                                              .map(Picture::getS3Path)
                                              .toList();
    EditProfileRequest updateInfo = EditProfileRequest.of(mainChange, main, changedProfileIds, existImagePaths, profilesInfo);
    Optional<Review> userCheck = reviewRepository.findByUserIdAndReviewType(id, ReviewType.EDIT_PROFILE);

    if (userCheck.isPresent()) {
      Review review = userCheck.get();
      if (review.getReviewStatus() == ReviewStatus.FAIL || review.getReviewStatus() == ReviewStatus.PENDING) {
        // 재심사인경우 s3 기존 이미지들 삭제
        log.info("[sendEditProfilesRequest] 기존 심사 요청 기록 삭제 ===> userId: {}", id);
        EditProfileRequest before = (EditProfileRequest) redisUtils.get(EDIT_PROFILE_KEY + id);
        s3Utils.deleteS3Image(before.newMainProfile());
        Set<String> profiles = before.newExtraProfiles()
                                     .keySet();
        for (String profile : profiles) {
          s3Utils.deleteS3Image(before.newExtraProfiles()
                                      .get(profile));
        }
      }
    }
    User user = getUserById(id);
    int EDIT_TOKEN_TIME = 10000;
    redisUtils.saveWithTTL(EDIT_PROFILE_KEY + id, updateInfo, EDIT_TOKEN_TIME, TimeUnit.SECONDS);
    makeJudgment(user, ReviewType.EDIT_PROFILE);
    sendNotificationToAdmin("회원 프로필 사진 변경 요청이 왔습니다.");
    log.info("[sendEditProfilesRequest] 회원 프로필 사진 변경 요청 완료. ===> userId: {}", id);
  }

  @Transactional
  public void updateUserForProfileEdit(EditProfileRequest request, Long userId) {
    User user = getUserById(userId);
    if (request.mainChange()) {
      log.info("[updateUserForProfileEdit] 메인 프로필 수정 완료 ===> userId: {}", userId);
      s3Utils.deleteS3Image(user.getMainProfile());
      user.updateMainProfile(request.newMainProfile());
    }
    List<Picture> changedPictures = pictureRepository.findByProfileIds(request.changedFileIds());
    changedPictures.forEach(picture -> s3Utils.deleteS3Image(picture.getS3Path()));
    pictureRepository.deleteProfileByIds(request.changedFileIds());
    saveEditImages(request.newExtraProfiles(), user);
    log.info("[updateUserForProfileEdit] 프로필 수정 완료 ===> userId: {}", userId);
    // FIXME 사용자에게 알림을 보내야함
  }

  @Transactional
  public void saveEditImages(Map<String, String> files, User user) {
    for (String originalFileName : files.keySet()) {
      Picture picture = Picture.builder()
                               .user(user)
                               .originalFileName(originalFileName)
                               .s3Path(files.get(originalFileName))
                               .build();
      pictureRepository.save(picture);
    }
  }

  @Transactional
  public void updateDetails(EditDetailsRequest request, Long userId) {
    UserDetail details = userDetailRepository.findByUserId(userId)
                                             .orElseThrow(() -> {
                                               log.error("[updateDetails] 해당 유저가 존재하지 않습니다. ===> userId: {}", userId);
                                               return new CustomException(UserErrorCode.USER_NOT_FOUND);
                                             });
    details.updateDetailsForEdit(request);
    log.info("[updateDetails] 세부 사항 수정 완료 ===> userId: {}", userId);
  }

  @Transactional
  public void updateUserAndDetails(Long id, boolean questionChange, boolean introduceChange, EditUserRequest editUserRequest,
                                   EditDetailsRequest details) {
    User user = getUserById(id);
    updateDetails(details, id);
    List<Picture> pictures = pictureRepository.findByUserId(id);
    List<String> picturesUrl = pictures.stream()
                                       .map(Picture::getS3Path)
                                       .toList();
    EditTotalRequest editTotalRequest = EditTotalRequest.of(editUserRequest, details, user.getMainProfile(), picturesUrl);
    int EDIT_TOKEN_TIME = 10000;
    redisUtils.saveWithTTL(EDIT_QUESTION_KEY + id, editTotalRequest, EDIT_TOKEN_TIME, TimeUnit.SECONDS);
    if (questionChange) {
      makeJudgment(user, ReviewType.EDIT_QUESTION);
      log.info("[updateUserAndDetails] 질문 수정 요청 성공 userId = {}", id);
    }
    if (introduceChange) {
      makeJudgment(user, ReviewType.EDIT_INTRODUCE);
      log.info("[updateUserAndDetails] 자기소개 수정 요청 성공 userId = {}", id);
    }
    sendNotificationToAdmin(String.format(USER_UPDATE_EDIT_REQUEST_MESSAGE, user.getUsername(), user.getEmail()));
  }

  @Transactional
  public void updateIntroduceOrQuestion(Long id, EditUserRequest userInfo, boolean isIntroduceChange) {
    User user = userRepository.findById(id)
                              .orElseThrow(() -> {
                                log.error("[updateIntroduceAndQuestion] 존재하지 않은 사용자입니다. ===> userId: {}", id);
                                return new CustomException(UserErrorCode.USER_NOT_FOUND);
                              });
    if (isIntroduceChange) {
      user.updateIntroduce(userInfo.introduce());
      log.info("[updateIntroduceOrQuestion] 자기소개 수정 완료 ===> userId: {}", id);
    }else {
      Question question = EditUserRequest.toQuestionEntity(userInfo, user);
      questionRepository.save(question);
      log.info("[updateIntroduceOrQuestion] 질문 수정 완료 ===> userId: {}", id);
    }
  }

  @Transactional
  public void save(User user) {
    userRepository.save(user);
  }

  public UserDetail getDetailsByUserIdFetchUser(Long userId) {
    return userDetailRepository.findByUserIdFetchUserInfo(userId)
                               .orElseThrow(() -> {
                                 log.error("[getDetailsByUserIdWithUser] 유저 상세 정보가 존재하지 않습니다. ===> userId: {}", userId);
                                 return new CustomException(UserErrorCode.USER_NOT_FOUND);
                               });
  }

  public UserDetailsResponse getAllDetails(Long userId) {
    Question question = questionRepository.findByUserIdOrderByCreatedDateDesc(userId)
                                          .orElseThrow(() -> {
                                            log.error("[getAllDetails] 존재하지 않은 질문입니다. ===> userId: {}", userId);
                                            return new CustomException(UserErrorCode.USER_NOT_FOUND);
                                          });
    UserDetail userDetail = getDetailsByUserIdFetchUser(userId);
    return UserDetailsResponse.of(userDetail, question);
  }

  public JoinTotalDetails getJoinDetails(Long userId) {
    UserDetail userDetail = userDetailRepository.findByUserIdFetchUserInfo(userId)
                                                .orElseThrow(() -> {
                                                  log.error("[getJoinDetails] 해당 유저가 존재하지 않습니다. ===> userId: {}", userId);
                                                  return new CustomException(UserErrorCode.USER_NOT_FOUND);
                                                });

    Question question = questionRepository.findByUserIdOrderByCreatedDateDesc(userId)
                                          .orElseThrow(() -> {
                                            log.error("[getJoinDetails] 해당 유저 질문 정보가 존재하지 않습니다. ===> userId: {}", userId);
                                            return new CustomException(UserErrorCode.USER_NOT_FOUND);
                                          });

    List<ProfileResponse> profiles = pictureRepository.findByUserId(userId)
                                                      .stream()
                                                      .map(ProfileResponse::of)
                                                      .toList();
    Integer totalThreadOptional = userAssetRepository.findTotalThreadByUserId(userId)
                                                     .orElseThrow(() -> {
                                                       log.error("[getJoinDetails] 해당 유저 실타레 정보가 존재하지 않습니다. ===> userId: {}", userId);
                                                       return new CustomException(UserErrorCode.USER_NOT_FOUND);
                                                     });
    log.info("[getJoinDetails] 회원 전체 정보 조회 성공 userId = {}", userId);
    return JoinTotalDetails.of(userDetail, profiles, question, totalThreadOptional);
  }

  public List<ProfileResponse> getProfiles(Long userId) {
    return pictureRepository.findByUserId(userId)
                            .stream()
                            .map(ProfileResponse::of)
                            .toList();
  }

  public User getUserById(Long userId) {
    return userRepository.findById(userId)
                         .orElseThrow(() -> {
                           log.info("[getUserById] 존재하지 않은 사용자입니다. ===> userId: {}", userId);
                           return new CustomException(UserErrorCode.USER_NOT_FOUND);
                         });
  }

  // 회원 탈퇴 로직 구현 (회원 상태가 WITHDRAWO_REQUEST 이고 30일이 지난 User ID 조회)
  public List<Long> getAllWithdrawUser() {
    return userRepository.findIdsByWithdrawRequestedBeforeDate(LocalDate.now().minusDays(30));
  }

  @Transactional
  public void withdrawUser(Long userId) {
    userDetailRepository.deleteByUserId(userId);
    pictureRepository.deleteByUserId(userId);
    blockRepository.deleteByUserId(userId);
    questionRepository.deleteByUserId(userId);
    fcmTokenRepository.deleteByUserId(userId);
    userRepository.deleteByUserId(userId);
  }

  private void sendNotificationToAdmin(String message) {
    User admin = userRepository.findAdminByEmailAndRole("admin", Role.ADMIN)
                               .orElseThrow(() -> {
                                 log.error("[sendNotificationToAdmin] 존재하지 않은 관리자입니다.");
                                 return new CustomException(UserErrorCode.ADMIN_NOT_FOUND);
                               });
    NotificationDto notificationDto = NotificationDto.of(NotificationType.ADMIN_MESSAGE_SENT, admin.getId(), admin.getUsername(), null, message,
                                                         LocalDateTime.now());
    notificationService.sendMessageTo(admin.getId(), notificationDto);
  }

  private List<User> getBlockedUsers(List<BlockInfoRequest> blockInfoRequests) {
    return userRepository.findBlockedUsersByPhoneNumberAndName(blockInfoRequests);
  }

  private Long completeFirstJoin(User user, UserDetail userDetail) {
    // 랜덤 코드 생성
    user.createCode(random.createRandomCode());
    userRepository.save(user);
    userDetailRepository.save(userDetail);
    log.debug("[completeFirstJoin] User, UserDetail 저장 완료 ===> userId: {}", user.getId());
    return user.getId();
  }

  private User getExistingUser(RejoinUserRequest request) {
    return userRepository.findByEmailAndCertificationFalse(request.email())
                         .orElseThrow(() -> {
                           log.error("[join] 심사 거부 이력이 없어 재가입 처리를 진행할 수 없습니다. ===> email = {}", request.email());
                           return new CustomException(UserErrorCode.REJECT_HISTORY_NOT_FOUND);
                         });
  }

  private void makeJudgment(User user, ReviewType type) {
    reviewRepository.deleteByUserIdAndReviewType(user.getId(), type);
    Review review = Review.builder()
                          .user(user)
                          .reviewType(type)
                          .reviewStatus(ReviewStatus.PENDING)
                          .reason(null)
                          .build();
    reviewRepository.save(review);
    log.debug("[makeJudgment] Judgment 저장 완료");
  }

  private User createNewUserFromRequest(RejoinUserRequest request, User existingUser, boolean mainChange, MultipartFile mainProfile) {
    User newUser = RejoinUserRequest.fromEntity(request);
    newUser.updateMainProfile(existingUser.getMainProfile());

    if (mainChange) {
      s3Utils.deleteS3Image(existingUser.getMainProfile());
      String newMain = s3Utils.saveImage(mainProfile);
      newUser.updateMainProfile(newMain);
    }

    return newUser;
  }

  private void updateExistingUserDetail(User existingUser, UserDetail newUserDetail) {
    UserDetail existingDetail = userDetailRepository.findByUserId(existingUser.getId())
                                                    .orElseThrow(() -> {
                                                      log.error("[updateExistingUserDetail] 이전 가입 이력이 존재하지 않습니다. email = {}",
                                                                existingUser.getEmail());
                                                      return new CustomException(UserErrorCode.JOIN_HISTORY_NOT_FOUND);
                                                    });

    existingDetail.updateDetailsForJoin(newUserDetail);
  }

  private void deleteOldPictures(List<Long> deleteFileIds) {
    if (deleteFileIds == null || deleteFileIds.isEmpty()) return;

    List<Picture> pictures = pictureRepository.findByProfileIds(deleteFileIds);
    pictures.forEach(p -> s3Utils.deleteS3Image(p.getS3Path()));
    pictureRepository.deleteProfileByIds(deleteFileIds);
  }

  private void saveNewProfilesIfNeeded(List<MultipartFile> newProfiles, User user) {
    if (newProfiles == null || newProfiles.isEmpty()) return;
    saveMultipartFileAndPictures(newProfiles, user);
  }

  private void saveMultipartFileAndPictures(List<MultipartFile> files, User newUser) {
    if (files == null) {
      return;
    }
    List<Picture> pictures = files.stream()
                                  .map(file -> Picture.builder()
                                                      .user(newUser)
                                                      .originalFileName(file.getOriginalFilename())
                                                      .s3Path(s3Utils.saveImage(file))
                                                      .build())
                                  .toList();
    pictureRepository.saveAll(pictures);
    log.debug("[saveMultipartFileAndMakePictures] S3 저장 완료 ===> picture size: {}", pictures.size());
  }

  public Optional<User> getUserBySocialId(Long socialId) {
    return userRepository.findBySocialId(socialId);
  }
}
