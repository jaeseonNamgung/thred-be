package com.thred.datingapp.admin.service;

import static com.thred.datingapp.common.error.errorCode.UserErrorCode.REVIEW_NOT_FOUND;
import static com.thred.datingapp.user.properties.RedisProperties.EDIT_INTRODUCE_KEY;
import static com.thred.datingapp.user.properties.RedisProperties.EDIT_PROFILE_KEY;
import static com.thred.datingapp.user.properties.RedisProperties.EDIT_QUESTION_KEY;

import com.thred.datingapp.admin.dto.response.ReviewResponse;
import com.thred.datingapp.chat.dto.NotificationDto;
import com.thred.datingapp.common.service.NotificationService;
import com.thred.datingapp.common.type.NotificationType;
import com.thred.datingapp.common.api.response.PageResponse;
import com.thred.datingapp.common.entity.admin.Review;
import com.thred.datingapp.common.entity.admin.ReviewStatus;
import com.thred.datingapp.common.entity.admin.ReviewType;
import com.thred.datingapp.common.entity.user.UserDetail;
import com.thred.datingapp.common.entity.user.Question;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.common.utils.RedisUtils;
import com.thred.datingapp.main.service.CardService;
import com.thred.datingapp.admin.repository.ReviewRepository;
import com.thred.datingapp.user.service.QuestionService;
import com.thred.datingapp.user.service.UserDetailService;
import com.thred.datingapp.user.service.UserService;
import com.thred.datingapp.user.properties.RedisProperties;
import com.thred.datingapp.main.dto.request.EditProfileRequest;
import com.thred.datingapp.main.dto.request.EditTotalRequest;
import com.thred.datingapp.main.dto.request.EditUserRequest;
import com.thred.datingapp.main.dto.response.UserDetailsResponse;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

  // 회원 가입 심사
  private static final String JOIN_REVIEW_SUCCESS_MESSAGE = "회원 가입 심사가 승인되었습니다.";
  private static final String JOIN_REVIEW_FAIL_MESSAGE    = "회원 가입 심사가 거부되었습니다.";

  // 프로필 수정 심사
  private static final String PROFILE_REVIEW_SUCCESS_MESSAGE = "프로필 수정 심사가 승인되었습니다.";
  private static final String PROFILE_REVIEW_FAIL_MESSAGE    = "프로필 수정 심사가 거부되었습니다.";

  // 질문 수정 심사
  private static final String QUESTION_REVIEW_SUCCESS_MESSAGE = "질문 수정 심사가 승인되었습니다.";
  private static final String QUESTION_REVIEW_FAIL_MESSAGE    = "질문 수정 심사가 거부되었습니다.";

  // 자기소개 수정 심사
  private static final String INTRODUCE_REVIEW_SUCCESS_MESSAGE = "자기소개 수정 심사가 승인되었습니다.";
  private static final String INTRODUCE_REVIEW_FAIL_MESSAGE    = "자기소개 수정 심사가 거부되었습니다.";

  private final ReviewRepository    reviewRepository;
  private final NotificationService notificationService;
  private final QuestionService     questionService;
  private final CardService         cardService;
  private final RedisUtils          redisUtils;
  private final UserService         userService;
  private final UserDetailService   userDetailService;

  @Transactional
  public void updateReview(final Long reviewId, final boolean check, final String reason) {
    Review review = getReviewFetchUser(reviewId);
    switch (review.getReviewType()) {
      case JOIN -> processJoinReview(review, check, reason);
      case EDIT_PROFILE -> processEditProfileReview(review, check, reason);
      case EDIT_QUESTION -> processEditQuestionReview(review, check, reason);
      case EDIT_INTRODUCE -> processEditIntroduce(review, check, reason);
    }
    reviewRepository.deleteById(review.getId());
  }

  public Review getJoinReviewByUserId(final Long userId) {
    return reviewRepository.findByUserIdAndReviewType(userId, ReviewType.JOIN).orElseThrow(() -> {
      log.error("[getJoinJudgmentResponse] 회원가입 심사 내역이 존재하지 않습니다. ==> userId: {}", userId);
      return new CustomException(UserErrorCode.REVIEW_NOT_FOUND);
    });
  }

  public PageResponse<ReviewResponse> getReview(final String reviewStatusStr, final Long pageLastId, final int pageSize) {

    ReviewStatus reviewStatus = ReviewStatus.findStatus(reviewStatusStr);
    Page<Review> reviewPage =
        reviewRepository.findByReviewStatusFetchUserOrderByCreatedDateDescWithPaging(reviewStatus, pageLastId, pageSize);
    log.info("[getReview] 심사 조회 성공 ===> reviewStatus: {}", reviewStatus);
    List<ReviewResponse> reviewResponses = reviewPage.getContent()
                                          .stream()
                                          .map(review -> ReviewResponse.of(review.getId(), review.getUser().getUsername(),
                                                                           review.getUser().getMainProfile(), review.getLastModifiedDate()))
                                          .toList();
    return  PageResponse.of(pageSize, reviewPage.isLast(), reviewResponses);
  }

  public UserDetailsResponse getReviewUserInfo(final Long reviewId) {
    Review review = getReviewFetchUser(reviewId);
    Long userId = review.getUser().getId();

    return switch (review.getReviewType()) {
      case JOIN -> getJoinReview(reviewId, userId);
      case EDIT_PROFILE -> getEditProfileReview(reviewId, userId);
      case EDIT_QUESTION -> getEditQuestionReview(reviewId, userId);
      case EDIT_INTRODUCE -> getEditIntroduceReview(reviewId, userId);
      default -> getDefaultSuccessReview(reviewId, review);
    };
  }

  private Review getReviewFetchUser(final Long reviewId) {
    return reviewRepository.findByIdFetchUser(reviewId)
                           .orElseThrow(() -> {
                             log.error("[getReviewUserInfo] 심사 내역이 존재하지 않습니다. ===> reviewId: {}", reviewId);
                             return new CustomException(UserErrorCode.REVIEW_NOT_FOUND);
                           });
  }

  private UserDetailsResponse getJoinReview(final Long reviewId, final Long userId) {
    Question question = questionService.getByUserId(userId);
    UserDetail userDetail = userDetailService.getByUserIdFetchUserInfo(userId);
    log.info("[getJoinReview] 회원가입 심사 정보 조회 완료 ===> reviewId: {}", reviewId);
    return UserDetailsResponse.of(userDetail, question);
  }

  private UserDetailsResponse getEditProfileReview(final Long reviewId, final Long userId) {
    EditProfileRequest editProfile = (EditProfileRequest) redisUtils.getValue(RedisProperties.EDIT_PROFILE_KEY + userId);
    Question question = questionService.getByUserId(userId);
    UserDetail userDetail = userDetailService.getByUserIdFetchUserInfo(userId);
    log.info("[getEditProfileReview] 프로필 수정 심사 정보 조회 완료 ===> reviewId: {}", reviewId);
    return UserDetailsResponse.of(editProfile, userDetail, question);
  }

  private UserDetailsResponse getEditQuestionReview(final Long reviewId, final Long userId) {
    EditTotalRequest editRequest = (EditTotalRequest) redisUtils.getValue(EDIT_QUESTION_KEY + userId);
    User user = userService.getUserById(userId);
    log.info("[getEditQuestionReview] 질문 수정 심사 정보 조회 완료 ===> reviewId: {}", reviewId);
    return UserDetailsResponse.of(editRequest, user, user.getIntroduce());
  }

  private UserDetailsResponse getEditIntroduceReview(final Long reviewId, final Long userId) {
    EditTotalRequest editRequest = (EditTotalRequest) redisUtils.getValue(EDIT_INTRODUCE_KEY + userId);
    User user = userService.getUserById(userId);
    Question question = questionService.getByUserId(userId);
    log.info("[getEditIntroduceReview] 자기소개 수정 심사 정보 조회 완료 ===> reviewId: {}", reviewId);
    return UserDetailsResponse.of(editRequest, user, question);
  }

  private UserDetailsResponse getDefaultSuccessReview(final Long reviewId, final Review review) {
    if (review.getReviewStatus() == ReviewStatus.SUCCESS) {
      Long userId = review.getUser().getId();
      Question question = questionService.getByUserId(userId);
      UserDetail userDetail = userDetailService.getByUserIdFetchUserInfo(userId);
      log.info("[getDefaultSuccessReview] 기타 타입 성공 심사 정보 조회 완료 ===> reviewId: {}", reviewId);
      return UserDetailsResponse.of(userDetail, question);
    }
    log.error("[getDefaultSuccessReview] 처리할 수 없는 심사 유형입니다. ===> reviewId: {}", reviewId);
    throw new CustomException(REVIEW_NOT_FOUND);
  }


  private void processEditIntroduce(final Review review, final boolean check, final String reason) {
    if (check) {
      EditTotalRequest editTotalRequest = (EditTotalRequest) redisUtils.getValue(EDIT_INTRODUCE_KEY + review.getUser().getId());
      Question question = questionService.getByUserId(review.getUser().getId());
      EditUserRequest editUserRequest =
          new EditUserRequest(editTotalRequest.user().introduce(), question.getQuestion1(), question.getQuestion2(), question.getQuestion3());
      userService.updateIntroduceOrQuestion(review.getUser().getId(), editUserRequest, true);
      redisUtils.deleteValue(EDIT_INTRODUCE_KEY + review.getUser().getId());
      review.updateReviewStatus(ReviewStatus.SUCCESS, null);
      sendReviewResultNotification(review.getUser(), INTRODUCE_REVIEW_SUCCESS_MESSAGE);
      log.info("[processEditIntroduce] 자기소개 수정 심사 승인 완료 ===> reviewId: {}, userId: {}", review.getId(), review.getUser().getId());
    } else {
      review.updateReviewStatus(ReviewStatus.FAIL, reason);
      log.info("[processEditIntroduce] 자기소개 수정 심사 거부 ===> reviewId: {}, userId: {}, reason: {}", review.getId(), review.getUser().getId(),
               review.getReason());
      sendReviewResultNotification(review.getUser(), INTRODUCE_REVIEW_FAIL_MESSAGE);
    }

  }

  private void processEditQuestionReview(final Review review, final boolean check, final String reason) {
    if (check) {
      EditTotalRequest editTotalRequest = (EditTotalRequest) redisUtils.getValue(EDIT_QUESTION_KEY + review.getUser().getId());
      EditUserRequest editUserRequest =
          new EditUserRequest(review.getUser().getIntroduce(), editTotalRequest.user().question1(), editTotalRequest.user().question2(),
                              editTotalRequest.user().question3());
      userService.updateIntroduceOrQuestion(review.getUser().getId(), editUserRequest, false);
      redisUtils.deleteValue(EDIT_QUESTION_KEY + review.getUser().getId());
      review.updateReviewStatus(ReviewStatus.SUCCESS, null);
      sendReviewResultNotification(review.getUser(), QUESTION_REVIEW_SUCCESS_MESSAGE);
      log.info("[processEditQuestionReview] 질문 수정 심사 승인 완료 ===> reviewId: {}, userId: {}", review.getId(), review.getUser().getId());
    } else {
      review.updateReviewStatus(ReviewStatus.FAIL, reason);
      sendReviewResultNotification(review.getUser(), QUESTION_REVIEW_FAIL_MESSAGE);
      log.info("[processEditQuestionReview] 질문 수정 심사 거부 ===> reviewId: {}, userId: {}, reason: {}", review.getId(), review.getUser().getId(),
               review.getReason());
    }

  }

  private void processEditProfileReview(final Review review, final boolean check, final String reason) {
    if (check) {
      EditProfileRequest editProfileRequest = (EditProfileRequest) redisUtils.getValue(EDIT_PROFILE_KEY + review.getUser().getId());
      userService.updateUserForProfileEdit(editProfileRequest, review.getUser().getId());
      review.updateReviewStatus(ReviewStatus.SUCCESS, null);
      redisUtils.deleteValue(EDIT_PROFILE_KEY + review.getUser().getId());
      sendReviewResultNotification(review.getUser(), PROFILE_REVIEW_SUCCESS_MESSAGE);
      log.info("[processEditProfileReview] 프로필 수정 심사 승인 완료 ===> reviewId: {}, userId: {}", review.getId(), review.getUser().getId());
    } else {
      review.updateReviewStatus(ReviewStatus.FAIL, reason);
      sendReviewResultNotification(review.getUser(), PROFILE_REVIEW_FAIL_MESSAGE);
      log.info("[processEditProfileReview] 프로필 수정 심사 거부 ===> reviewId: {}, userId: {}, reason: {}", review.getId(), review.getUser().getId(),
               review.getReason());
    }

  }

  private void processJoinReview(final Review review, final boolean check, final String reason) {
    if (check) {
      userService.changeJoinStatus(review.getUser().getId(), true);
      if (review.getUser().getInputCode() != null) {
        userService.joinCodeEvent(review.getUser().getId(), review.getUser().getInputCode());
      }
      review.updateReviewStatus(ReviewStatus.SUCCESS, null);
      cardService.createCard(review.getUser().getId());
      sendReviewResultNotification(review.getUser(), JOIN_REVIEW_SUCCESS_MESSAGE);
      log.info("[processJoinReview] 회원가입 심사 승인 완료 ===> reviewId: {}, userId: {}", review.getId(), review.getUser().getId());
    } else {
      userService.changeJoinStatus(review.getUser().getId(), false);
      review.updateReviewStatus(ReviewStatus.FAIL, reason);
      sendReviewResultNotification(review.getUser(), JOIN_REVIEW_FAIL_MESSAGE);
      log.info("[processJoinReview] 회원가입 심사 거부 ===> reviewId: {}, userId: {}, reason: {}", review.getId(), review.getUser().getId(), review.getReason());
    }
  }

  private void sendReviewResultNotification(final User user, final String message) {
    NotificationDto notificationDto =
        NotificationDto.of(NotificationType.MESSAGE_SENT, user.getId(), user.getUsername(), null, message, LocalDateTime.now());
    notificationService.sendMessageTo(user.getId(), notificationDto);
  }
}
