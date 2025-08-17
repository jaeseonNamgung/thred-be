package com.thred.datingapp.join.service;

import com.testFixture.UserFixture;
import com.thred.datingapp.admin.repository.ReviewRepository;
import com.thred.datingapp.chat.dto.NotificationDto;
import com.thred.datingapp.chat.repository.FcmTokenRepository;
import com.thred.datingapp.common.service.NotificationService;
import com.thred.datingapp.common.entity.admin.Review;
import com.thred.datingapp.common.entity.admin.ReviewType;
import com.thred.datingapp.common.entity.chat.FcmToken;
import com.thred.datingapp.common.entity.user.*;
import com.thred.datingapp.common.entity.user.field.*;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.common.utils.S3Utils;
import com.thred.datingapp.user.api.request.BlockInfoRequest;
import com.thred.datingapp.user.api.request.JoinDetailsRequest;
import com.thred.datingapp.user.api.request.JoinUserRequest;
import com.thred.datingapp.user.api.request.RejoinUserRequest;
import com.thred.datingapp.user.repository.*;
import com.thred.datingapp.user.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  private UserService                  sut;
  @Mock
  private UserRepository               userRepository;
  @Mock
  private PictureService               pictureService;
  @Mock
  private UserDetailService            userDetailService;
  @Mock
  private BlockService                 blockService;
  @Mock
  private QuestionService              questionService;
  @Mock
  private ReviewRepository             judgmentsRepository;
  @Mock
  private FcmTokenService              fcmTokenService;
  @Mock
  private RandomStringGeneratorService random;
  @Mock
  private S3Utils                      s3Utils;
  @Mock
  private NotificationService          notificationService;


  /*
   * 1. 이미 가입한 회원이 존재할 때
   * 2. 정상적으로 회원가입 프로세스가 끝날 때
   * */

  @Test
  @DisplayName("[join] 이미 존재하는 회원이 있다면 AlreadyJoin 에러가 발생한다.")
  void givenExistingMember_whenJoining_thenThrowsAlreadyJoinError() {
    // given
    JoinDetailsRequest joinDetailsRequest = createJoinDetailsRequest();
    JoinUserRequest joinUserRequest = createJoinUserRequest();
    MockMultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());

    given(userRepository.existsByEmail(anyString())).willReturn(true);
    // when
    CustomException exception =
        assertThrows(CustomException.class, () -> sut.join(joinUserRequest, joinDetailsRequest, multipartFile, List.of(multipartFile)));
    // then
    assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.ALREADY_REGISTERED_USER);

    then(userRepository).should().existsByEmail(anyString());
  }

  @Test
  @DisplayName("[join] 정상적으로 회원가입 프로세스가 끝날면 userId를 반환한다.")
  void givenValidUserInfo_whenJoining_thenReturnsUserId() {
    // given
    JoinDetailsRequest joinDetailsRequest = createJoinDetailsRequest();
    JoinUserRequest joinUserRequest = createJoinUserRequest();
    MockMultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());

    User admin = UserFixture.createAdmin();
    ReflectionTestUtils.setField(admin, "id", 1L);

    given(userRepository.existsByEmail(anyString())).willReturn(false);
    given(userRepository.save(any(User.class))).willAnswer(invocation -> {
      User u = invocation.getArgument(0, User.class);
      ReflectionTestUtils.setField(u, "id", 1L);
      return u;
    });
    given(s3Utils.saveImage(any(MultipartFile.class))).willReturn("decodedMainProfileUrl");
    given(random.createRandomCode()).willReturn("123456");
    given(userRepository.findAdminByEmailAndRole(anyString(), any(Role.class))).willReturn(Optional.of(admin));

    // when
    Long userId = sut.join(joinUserRequest, joinDetailsRequest, multipartFile, List.of(multipartFile));
    // then
    assertThat(userId).isNotNull();

    then(userRepository).should().existsByEmail(anyString());
    then(s3Utils).should(times(2)).saveImage(any(MultipartFile.class));
    then(userRepository).should().save(any(User.class));
    then(userDetailService).should().save(any(UserDetail.class));
    then(pictureService).should().saveAll(any());
    then(questionService).should().save(any());
    then(fcmTokenService).should().save(any(FcmToken.class));
    then(random).should().createRandomCode();
    then(judgmentsRepository).should().deleteByUserIdAndReviewType(anyLong(), any(ReviewType.class));
    then(judgmentsRepository).should().save(any(Review.class));
    then(userRepository).should().findAdminByEmailAndRole(anyString(), any(Role.class));
    then(notificationService).should().sendMessageTo(anyLong(), any(NotificationDto.class));
  }

  @Test
  @DisplayName("존재하지 않은 회원일 경우 NOT_EXIST_USER 에러 발생한다")
  void givenInValidEmail_whenGetExistingUser_thenThrowsNotExistUserError() {
    // given
    RejoinUserRequest rejoinUserRequest = createRejoinUserRequest();
    JoinDetailsRequest joinDetailsRequest = createJoinDetailsRequest();
    MockMultipartFile mainProfile = new MockMultipartFile("mainProfile", "new-main.jpg", "image/jpeg", "image-data".getBytes());

    MockMultipartFile profile1 = new MockMultipartFile("profile1", "profile1.jpg", "image/jpeg", "profile-data-1".getBytes());
    MockMultipartFile profile2 = new MockMultipartFile("profile2", "profile2.jpg", "image/jpeg", "profile-data-2".getBytes());

    List<MultipartFile> newProfiles = List.of(profile1, profile2);
    given(userRepository.findByEmailAndCertificationFalse(anyString())).willReturn(Optional.empty());
    // when
    CustomException exception = assertThrows(CustomException.class,
                                             () -> sut.rejoin(rejoinUserRequest, joinDetailsRequest, true, mainProfile, List.of(1L, 2L, 3L),
                                                              newProfiles));
    // then
    assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.REJECT_HISTORY_NOT_FOUND);
    then(userRepository).should().findByEmailAndCertificationFalse(anyString());
  }

  @Test
  @DisplayName("회원 재가입 성공 테스트")
  void givenValidRequest_whenRejoin_thenReturnsVoid() {
    // given
    RejoinUserRequest rejoinUserRequest = createRejoinUserRequest();
    JoinDetailsRequest joinDetailsRequest = createJoinDetailsRequest();
    MockMultipartFile mainProfile = new MockMultipartFile("mainProfile", "new-main.jpg", "image/jpeg", "image-data".getBytes());

    MockMultipartFile profile1 = new MockMultipartFile("profile1", "profile1.jpg", "image/jpeg", "profile-data-1".getBytes());
    MockMultipartFile profile2 = new MockMultipartFile("profile2", "profile2.jpg", "image/jpeg", "profile-data-2".getBytes());
    List<MultipartFile> newProfiles = List.of(profile1, profile2);
    User user = UserFixture.createTestUser(1);
    ReflectionTestUtils.setField(user, "id", 1L);
    User admin = UserFixture.createAdmin();
    ReflectionTestUtils.setField(admin, "id", 1L);

    given(userRepository.findByEmailAndCertificationFalse(anyString())).willReturn(Optional.of(user));
    given(userDetailService.getByUserId(anyLong())).willReturn(UserFixture.createDetails1(user));
    given(pictureService.getAllByProfileIds(anyList())).willReturn(UserFixture.createPictures(2, user));
    given(userRepository.findAdminByEmailAndRole(anyString(), any(Role.class))).willReturn(Optional.of(admin));
    // when
    sut.rejoin(rejoinUserRequest, joinDetailsRequest, true, mainProfile, List.of(1L, 2L, 3L), newProfiles);
    // then

    then(userRepository).should().findByEmailAndCertificationFalse(anyString());
    then(s3Utils).should(times(3)).deleteS3Image(anyString());
    then(s3Utils).should(times(3)).saveImage(any(MultipartFile.class));
    then(userDetailService).should().getByUserId(anyLong());
    then(pictureService).should().getAllByProfileIds(anyList());
    then(pictureService).should().deleteProfileByIds(anyList());
    then(pictureService).should().saveAll(anyList());
    then(questionService).should().deleteByUserId(anyLong());
    then(questionService).should().save(any(Question.class));
    then(judgmentsRepository).should().deleteByUserIdAndReviewType(anyLong(), any(ReviewType.class));
    then(judgmentsRepository).should().save(any(Review.class));
    then(userRepository).should().findAdminByEmailAndRole(anyString(), any(Role.class));
    then(notificationService).should().sendMessageTo(anyLong(), any(NotificationDto.class));
  }

  @Test
  @DisplayName("회원이 존재하지 않으면 NotExistUser 에러가 발생한다.")
  void givenInvalidUserId_whenSetBlockNumber_thenThrowsNotExistUser() {
    // given
    List<BlockInfoRequest> blocks = createBlocks();
    given(userRepository.findById(anyLong())).willReturn(Optional.empty());
    // when
    CustomException exception = assertThrows(CustomException.class, () -> sut.setBlockNumber(1L, blocks));
    // then
    assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    then(userRepository).should().findById(anyLong());
  }

  @Test
  @DisplayName("setBlockNumber 성공 테스트")
  void givenValidUserIdAndRequest_whenSetBlockNumber_thenReturnVoid() {
    // given
    List<BlockInfoRequest> blocks = createBlocks();
    User user = UserFixture.createTestUser(1);
    List<User> blockedUsers = IntStream.rangeClosed(2, 6).mapToObj(UserFixture::createTestUser).toList();
    given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
    given(userRepository.findBlockedUsersByPhoneNumberAndName(any())).willReturn(blockedUsers);
    // when
    sut.setBlockNumber(1L, blocks);
    // then
    then(userRepository).should().findById(anyLong());
    then(userRepository).should().findBlockedUsersByPhoneNumberAndName(any());
    then(blockService).should().deleteByBlockerId(anyLong());
    then(blockService).should().bulkInsert(any(User.class), anyList());
  }

  private static List<BlockInfoRequest> createBlocks() {
    return List.of(new BlockInfoRequest("김민수", "010-1234-5678"), new BlockInfoRequest("이영희", "010-2345-6789"),
                   new BlockInfoRequest("박준형", "010-3456-7890"), new BlockInfoRequest("최지우", "010-4567-8901"),
                   new BlockInfoRequest("정해인", "010-5678-9012"));
  }

  public static JoinUserRequest createJoinUserRequest() {
    return new JoinUserRequest(1L, "test@example.com", Gender.MALE.getGender(), "테스트유저", "서울시", "서울특별시", "1990-01-01", "안녕하세요, 자기소개입니다.", "첫 번째 질문",
                               "두 번째 질문", "세 번째 질문", "010-1234-5678", "CODE123", PartnerGender.OTHER.getGender(), "fcm-token-value");
  }

  private static RejoinUserRequest createRejoinUserRequest() {
    return new RejoinUserRequest("test@example.com", Gender.MALE.getGender(), "테스트유저", "서울시", "서울특별시", "1990-01-01", "안녕하세요, 자기소개입니다.", "첫 번째 질문",
                                 "두 번째 질문", "세 번째 질문", "010-1234-5678", "CODE123", PartnerGender.OTHER.getGender());
  }

  public static JoinDetailsRequest createJoinDetailsRequest() {
    return new JoinDetailsRequest(175, Drink.ENJOY.getDrink(), Smoke.NONE.getSmoke(), Belief.NON_RELIGIOUS.getBelief(),
                                  OppositeFriends.A_LITTLE_BIT.getAmount(), Job.OFFICE_WORKER.getJob(), Mbti.ESFJ.getMbti(), 70);
  }

}
