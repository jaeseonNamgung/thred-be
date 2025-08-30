package com.thred.datingapp.user.service;

import static com.thred.datingapp.user.properties.RedisProperties.EDIT_PROFILE_KEY;
import static com.thred.datingapp.user.properties.RedisProperties.EDIT_QUESTION_KEY;

import com.thred.datingapp.admin.repository.ReviewRepository;
import com.thred.datingapp.chat.dto.NotificationDto;
import com.thred.datingapp.chat.repository.FcmTokenRepository;
import com.thred.datingapp.common.annotation.DistributedLock;
import com.thred.datingapp.common.entity.user.field.LoginType;
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
import com.thred.datingapp.common.utils.PhoneNumberUtils;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
  private final UserDetailService            userDetailService;
  private final QuestionService              questionService;
  private final S3Utils                      s3Utils;
  private final RedisUtils                   redisUtils;
  private final PictureService               pictureService;
  private final FcmTokenService              fcmTokenService;
  private final UserAssetService             userAssetService;
  private final UserRepository               userRepository;

  public boolean checkDuplicateEmail(final String email) {
    return userRepository.findByEmail(email).isPresent();
  }

  public boolean checkDuplicateName(final String username) {
    return userRepository.existsByUsername(username);
  }

  public boolean checkCode(final String code) {
    return userRepository.existsByCode(code);
  }

  @Transactional
  @DistributedLock(key = "#joinUserRequest.socialId", waitTime = 5, leaseTime = 10)
  public Long join(final JoinUserRequest joinUserRequest,
                   final JoinDetailsRequest details,
                   final MultipartFile mainProfile,
                   final List<MultipartFile> files) {
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
    questionService.save(question);
    // 4. 회원 프로필 이미지 저장
    saveMultipartFileAndPictures(files, newUser);
    // 5. Judgment 저장
    makeReview(newUser, ReviewType.JOIN);
    // 6. FCM 정보 저장
    FcmToken fcmToken = FcmToken.builder().token(joinUserRequest.fcmToken()).member(newUser).build();
    fcmTokenService.save(fcmToken);
    // 7. FCM 알림 전송
    sendNotificationToAdmin(String.format(USER_REGISTRATION_REQUEST_MESSAGE, newUser.getUsername(), newUser.getEmail()));
    log.info("[join] 회원가입 요청 완료 ===> email: {}", joinUserRequest.email());
    return userId;
  }

  @Transactional
  public void rejoin(final RejoinUserRequest rejoinUserRequest,
                     final JoinDetailsRequest details,
                     final boolean mainChange,
                     final MultipartFile mainProfile,
                     final List<Long> deleteFileIds,
                     final List<MultipartFile> newProfiles) {

    // 1. 기존 회원 조회
    User existingUser = getExistingUser(rejoinUserRequest);
    // 2. Main 프로필 업데이트
    User newUser = createNewUserFromRequest(rejoinUserRequest, existingUser, mainChange, mainProfile);
    // 3. 회원 detail 업데이트
    UserDetail newUserDetail = JoinDetailsRequest.fromEntity(details, newUser);
    updateExistingUserDetail(existingUser.getId(), newUserDetail);
    // 4. 기존 프로필 삭제 및 새 프로필 업데이트
    deleteOldPictures(deleteFileIds);
    saveNewProfilesIfNeeded(newProfiles, existingUser);
    // 5. 기존 질문 삭제 및 새 질문 저장
    questionService.deleteByUserId(existingUser.getId());
    questionService.save(RejoinUserRequest.fromEntity(rejoinUserRequest, existingUser));
    // 6. Judgment 생성
    makeReview(existingUser, ReviewType.JOIN);
    log.info("[rejoin] 재가입 요청 완료 ===> email: {}", rejoinUserRequest.email());
    // 7. FCM 알림 전송
    sendNotificationToAdmin(USER_REGISTRATION_REQUEST_MESSAGE);
  }

  @Transactional
  public void changeJoinStatus(final Long userId, final boolean result) {
    User user = getUserById(userId);
    if (result) {
      user.successJoin();
    } else {
      user.failJoin();
    }
    log.info("[changeJoinResult] 회원 가입 결과 업데이트 완료 ===> userId: {}", userId);
  }

  @Transactional
  public void changePhoneNumber(final Long id, final String number) {
    User user = getUserById(id);
    user.changePhoneNumber(PhoneNumberUtils.toE164Format(number));
    log.info("[changePhoneNumber] 핸드폰 번호 수정 완료 ===> userId: {}", id);
  }

  @Transactional
  public void changeAddress(final Long id, final String city, final String province) {
    User user = getUserById(id);
    user.changeAddress(city, province);
    String key = "card:daily:viewer:" + user.getId() + ":" + LocalDate.now();
    redisUtils.deleteValue(key);
    log.info("[changeAddress] 주소 정보 수정 완료 ===> userId: {}", id);
  }

  @Transactional
  public void sendEditProfilesRequest(final Long userId,
                                      final boolean mainChange,
                                      final MultipartFile mainProfile,
                                      final List<Long> changedProfileIds,
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
      existImages = pictureService.getAllByUserIdAndIdNotIn(userId, changedProfileIds);
    } else {
      existImages = pictureService.getAllByUserId(userId);
    }

    List<String> existImagePaths = existImages.stream().map(Picture::getS3Path).toList();
    EditProfileRequest updateInfo = EditProfileRequest.of(mainChange, main, changedProfileIds, existImagePaths, profilesInfo);
    Optional<Review> userCheck = reviewRepository.findByUserIdAndReviewType(userId, ReviewType.EDIT_PROFILE);

    if (userCheck.isPresent()) {
      Review review = userCheck.get();
      if (review.getReviewStatus() == ReviewStatus.FAIL || review.getReviewStatus() == ReviewStatus.PENDING) {
        // 재심사인경우 s3 기존 이미지들 삭제
        log.info("[sendEditProfilesRequest] 기존 심사 요청 기록 삭제 ===> userId: {}", userId);
        EditProfileRequest before = (EditProfileRequest) redisUtils.get(EDIT_PROFILE_KEY + userId);
        s3Utils.deleteS3Image(before.newMainProfile());
        Set<String> profiles = before.newExtraProfiles().keySet();
        for (String profile : profiles) {
          s3Utils.deleteS3Image(before.newExtraProfiles().get(profile));
        }
      }
    }
    User user = getUserById(userId);
    int EDIT_TOKEN_TIME = 10000;
    redisUtils.saveWithTTL(EDIT_PROFILE_KEY + userId, updateInfo, EDIT_TOKEN_TIME, TimeUnit.SECONDS);
    makeReview(user, ReviewType.EDIT_PROFILE);
    sendNotificationToAdmin("회원 프로필 사진 변경 요청이 왔습니다.");
    log.info("[sendEditProfilesRequest] 회원 프로필 사진 변경 요청 완료. ===> userId: {}", userId);
  }

  @Transactional
  public void updateUserForProfileEdit(final EditProfileRequest request, final Long userId) {
    User user = getUserById(userId);
    if (request.mainChange()) {
      log.info("[updateUserForProfileEdit] 메인 프로필 수정 완료 ===> userId: {}", userId);
      s3Utils.deleteS3Image(user.getMainProfile());
      user.updateMainProfile(request.newMainProfile());
    }
    List<Picture> changedPictures = pictureService.getAllByProfileIds(request.changedFileIds());
    changedPictures.forEach(picture -> s3Utils.deleteS3Image(picture.getS3Path()));
    pictureService.deleteProfileByIds(request.changedFileIds());
    saveEditImages(request.newExtraProfiles(), user);
    log.info("[updateUserForProfileEdit] 프로필 수정 완료 ===> userId: {}", userId);
    // FIXME 사용자에게 알림을 보내야함
  }

  @Transactional
  public void saveEditImages(final Map<String, String> files, final User user) {
    for (String originalFileName : files.keySet()) {
      Picture picture = Picture.builder().user(user).originalFileName(originalFileName).s3Path(files.get(originalFileName)).build();
      pictureService.save(picture);
    }
  }

  @Transactional
  public void updateDetails(final EditDetailsRequest request, final Long userId) {
    UserDetail details = userDetailService.getByUserId(userId);
    details.updateDetailsForEdit(request);
    log.info("[updateDetails] 세부 사항 수정 완료 ===> userId: {}", userId);
  }

  @Transactional
  public void updateUserAndDetails(final Long id,
                                   final boolean questionChange,
                                   final boolean introduceChange,
                                   final EditUserRequest editUserRequest,
                                   EditDetailsRequest details) {
    User user = getUserById(id);
    updateDetails(details, id);
    List<Picture> pictures = pictureService.getAllByUserId(id);
    List<String> picturesUrl = pictures.stream().map(Picture::getS3Path).toList();
    EditTotalRequest editTotalRequest = EditTotalRequest.of(editUserRequest, details, user.getMainProfile(), picturesUrl);
    int EDIT_TOKEN_TIME = 10000;
    redisUtils.saveWithTTL(EDIT_QUESTION_KEY + id, editTotalRequest, EDIT_TOKEN_TIME, TimeUnit.SECONDS);
    if (questionChange) {
      makeReview(user, ReviewType.EDIT_QUESTION);
      log.info("[updateUserAndDetails] 질문 수정 요청 성공 userId = {}", id);
    }
    if (introduceChange) {
      makeReview(user, ReviewType.EDIT_INTRODUCE);
      log.info("[updateUserAndDetails] 자기소개 수정 요청 성공 userId = {}", id);
    }
    sendNotificationToAdmin(String.format(USER_UPDATE_EDIT_REQUEST_MESSAGE, user.getUsername(), user.getEmail()));
  }

  @Transactional
  public void updateIntroduceOrQuestion(final Long id, final EditUserRequest userInfo, final boolean isIntroduceChange) {
    User user = userRepository.findById(id).orElseThrow(() -> {
      log.error("[updateIntroduceAndQuestion] 존재하지 않은 사용자입니다. ===> userId: {}", id);
      return new CustomException(UserErrorCode.USER_NOT_FOUND);
    });
    if (isIntroduceChange) {
      user.updateIntroduce(userInfo.introduce());
      log.info("[updateIntroduceOrQuestion] 자기소개 수정 완료 ===> userId: {}", id);
    } else {
      Question question = EditUserRequest.toQuestionEntity(userInfo, user);
      questionService.save(question);
      log.info("[updateIntroduceOrQuestion] 질문 수정 완료 ===> userId: {}", id);
    }
  }

  @Transactional
  public void save(final User user) {
    userRepository.save(user);
  }

  public UserDetailsResponse getAllDetails(final Long userId) {
    Question question = questionService.getByUserId(userId);
    UserDetail userDetail = userDetailService.getByUserIdFetchUserInfo(userId);
    return UserDetailsResponse.of(userDetail, question);
  }

  public JoinTotalDetails getJoinDetails(final Long userId) {
    UserDetail userDetail = userDetailService.getByUserIdFetchUserInfo(userId);
    Question question = questionService.getByUserId(userId);

    List<ProfileResponse> profiles = pictureService.getAllByUserId(userId).stream().map(ProfileResponse::of).toList();
    int totalThread = userAssetService.getTotalThreadByUserId(userId);
    log.info("[getJoinDetails] 회원 전체 정보 조회 성공 userId = {}", userId);
    return JoinTotalDetails.of(userDetail, profiles, question, totalThread);
  }

  public List<ProfileResponse> getProfiles(final Long userId) {
    return pictureService.getAllByUserId(userId).stream().map(ProfileResponse::of).toList();
  }

  public User getUserById(final Long userId) {
    return userRepository.findById(userId).orElseThrow(() -> {
      log.info("[getUserById] 존재하지 않은 사용자입니다. ===> userId: {}", userId);
      return new CustomException(UserErrorCode.USER_NOT_FOUND);
    });
  }

  // 회원 탈퇴 로직 구현 (회원 상태가 WITHDRAWO_REQUEST 이고 30일이 지난 User ID 조회)
  public List<Long> getAllWithdrawUser() {
    return userRepository.findIdsByWithdrawRequestedBeforeDate(LocalDate.now().minusDays(30));
  }

  @Transactional
  public void deleteByUserId(final Long userId) {
    if (userId == null) {
      log.error("[deleteByUserId] userId is Null");
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }
    userRepository.deleteByUserId(userId);
  }

  public void checkExistsUser(final Long userId) {
    boolean existUser = userRepository.existsById(userId);
    if (!existUser) {
      log.error("[checkExistsUser] 존재하지 않은 회원 (Not exist user) ===> userId: {}", userId);
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }
  }

  public List<User> getAllBlockedUsersByPhoneNumberAndName(final List<BlockInfoRequest> blockInfoRequests) {
    return userRepository.findBlockedUsersByPhoneNumberAndName(blockInfoRequests);
  }

  public Optional<User> getUserBySocialId(final Long socialId) {
    return userRepository.findBySocialId(socialId);
  }

  public Optional<User> getUserByPhoneNumberAndLoginType(final String phoneNumber, final LoginType loginType) {
    return userRepository.findByPhoneNumberAndLoginType(phoneNumber, loginType);
  }

  public User getByCode(final String code) {
    return userRepository.findByCode(code).orElseThrow(() -> {
      log.error("[getByCode] 존재하지 않은 코드입니다.(Not exist user) ===> referralCode: {}", code);
      return new CustomException(UserErrorCode.INVALID_CODE);
    });
  }

  private void sendNotificationToAdmin(final String message) {
    User admin = userRepository.findAdminByEmailAndRole("admin", Role.ADMIN).orElseThrow(() -> {
      log.error("[sendNotificationToAdmin] 존재하지 않은 관리자입니다.");
      return new CustomException(UserErrorCode.ADMIN_NOT_FOUND);
    });
    NotificationDto notificationDto =
        NotificationDto.of(NotificationType.ADMIN_MESSAGE_SENT, admin.getId(), admin.getUsername(), null, message, LocalDateTime.now());
    notificationService.sendMessageTo(admin.getId(), notificationDto);
  }

  private Long completeFirstJoin(final User user, final UserDetail userDetail) {
    // 랜덤 코드 생성
    user.createCode(random.createRandomCode());
    userRepository.save(user);
    userDetailService.save(userDetail);
    log.debug("[completeFirstJoin] User, UserDetail 저장 완료 ===> userId: {}", user.getId());
    return user.getId();
  }

  private User getExistingUser(final RejoinUserRequest request) {
    return userRepository.findByEmailAndCertificationFalse(request.email()).orElseThrow(() -> {
      log.error("[join] 심사 거부 이력이 없어 재가입 처리를 진행할 수 없습니다. ===> email = {}", request.email());
      return new CustomException(UserErrorCode.REJECT_HISTORY_NOT_FOUND);
    });
  }

  private void makeReview(final User user, final ReviewType type) {
    reviewRepository.deleteByUserIdAndReviewType(user.getId(), type);
    Review review = Review.builder().user(user).reviewType(type).reviewStatus(ReviewStatus.PENDING).reason(null).build();
    reviewRepository.save(review);
    log.debug("[makeReview] Review 저장 완료");
  }

  private User createNewUserFromRequest(final RejoinUserRequest request,
                                        final User existingUser,
                                        final boolean mainChange,
                                        final MultipartFile mainProfile) {
    User newUser = RejoinUserRequest.fromEntity(request);
    newUser.updateMainProfile(existingUser.getMainProfile());

    if (mainChange) {
      s3Utils.deleteS3Image(existingUser.getMainProfile());
      String newMain = s3Utils.saveImage(mainProfile);
      newUser.updateMainProfile(newMain);
    }

    return newUser;
  }

  private void updateExistingUserDetail(final Long userId, final UserDetail newUserDetail) {
    UserDetail existingDetail = userDetailService.getByUserId(userId);
    existingDetail.updateDetailsForJoin(newUserDetail);
  }

  private void deleteOldPictures(final List<Long> deleteFileIds) {
    if (deleteFileIds == null || deleteFileIds.isEmpty()) return;

    List<Picture> pictures = pictureService.getAllByProfileIds(deleteFileIds);
    pictures.forEach(p -> s3Utils.deleteS3Image(p.getS3Path()));
    pictureService.deleteProfileByIds(deleteFileIds);
  }

  private void saveNewProfilesIfNeeded(final List<MultipartFile> newProfiles, final User user) {
    if (newProfiles == null || newProfiles.isEmpty()) return;
    saveMultipartFileAndPictures(newProfiles, user);
  }

  private void saveMultipartFileAndPictures(final List<MultipartFile> files, final User newUser) {
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
    pictureService.saveAll(pictures);
    log.debug("[saveMultipartFileAndMakePictures] S3 저장 완료 ===> picture size: {}", pictures.size());
  }
}
