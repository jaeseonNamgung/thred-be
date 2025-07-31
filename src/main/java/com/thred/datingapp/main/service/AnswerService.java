package com.thred.datingapp.main.service;

import static com.thred.datingapp.common.error.errorCode.MainErrorCode.QUESTION_NOT_FOUND;

import com.thred.datingapp.common.entity.Answer;
import com.thred.datingapp.common.entity.user.Question;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.main.dto.response.AnswerAllResponse;
import com.thred.datingapp.main.repository.AnswerRepository;
import com.thred.datingapp.user.repository.QuestionRepository;
import com.thred.datingapp.main.dto.request.AnswerRequest;

import java.util.Optional;

import com.thred.datingapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerService {

  private final AnswerRepository   answerRepository;
  private final UserRepository     userRepository;
  private final QuestionRepository questionRepository;

  @Transactional
  public void saveAnswer(Long senderId, Long receiverId, AnswerRequest answerRequest) {
    User sender = getUserById(senderId);
    User receiver = getUserById(receiverId);

    Question question = questionRepository.findByUserId(receiverId).orElseThrow(() -> {
      log.error("[saveAnswer] 질문지가 존재하지 않습니다. ===> receiverId: {}", receiverId);
      return new CustomException(QUESTION_NOT_FOUND);
    });
    answerRepository.save(AnswerRequest.toEntity(answerRequest, sender, receiver, question));
    log.info("[saveAnswer] 답변 저장 완료. senderId = {}, receiverId = {}", senderId, receiverId);
  }

  public boolean isSend(Long senderId, Long receiverId) {
    Optional<Answer> optionalAnswer = answerRepository.findBySenderIdAndReceiverId(senderId, receiverId);
    log.info("[isSend] 답변 저장 여부 조회 완료. ===> senderId: {}, receiverId: {}", senderId, receiverId);
    return optionalAnswer.isPresent();
  }

  public AnswerAllResponse getSubmittedAnswer(Long senderId, Long receiverId) {
    String answerTargetUsername = userRepository.getUsernameById(receiverId);
    Question question = getQuestionByUserId(receiverId);
    return answerRepository.findBySenderIdAndReceiverId(senderId, receiverId)
                           .map(answer -> AnswerAllResponse.of(question, answer, answerTargetUsername))
                           .orElseGet(() -> AnswerAllResponse.of(question, null, answerTargetUsername));
  }

  private User getUserById(Long userId) {
    return userRepository.findById(userId).orElseThrow(() -> {
      log.error("[getUserById] 존재하지 않은 사용자입니다. ===> userId: {}", userId);
      return new CustomException(UserErrorCode.USER_NOT_FOUND);
    });
  }

  private Question getQuestionByUserId(Long userId) {
    return questionRepository.findByUserId(userId).orElseThrow(() -> {
      log.error("[getQuestionByUserId] 존재하지 않은 Question 입니다. ===> userId: {}", userId);
      return new CustomException(QUESTION_NOT_FOUND);
    });
  }

}
