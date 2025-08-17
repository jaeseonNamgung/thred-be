package com.thred.datingapp.user.service;

import com.thred.datingapp.common.entity.user.Question;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.ProfileErrorCode;

import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.user.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class QuestionService {

  private final QuestionRepository questionRepository;

  public Question getByUserId(Long userId) {
    return questionRepository.findByUserId(userId)
                             .orElseThrow(() -> {
                               log.error("[findQuestionByUserId] 존재하지 않은 질문 정보입니다. ===> userId: {}", userId);
                               return new CustomException(ProfileErrorCode.QUESTION_NOT_FOUND);
                             });
  }

  @Transactional
  public void save(Question question) {
    questionRepository.save(question);
    log.debug("[save] question 저장 완료 ===> questionId: {}", question.getId());
  }

  @Transactional
  public void deleteByUserId(Long userId) {

    if(userId == null) {
      log.error("[deleteById] userId is Null");
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }
    questionRepository.deleteById(userId);
    log.debug("[deleteById] question 삭제 완료 ===> userId: {}", userId);
  }
}
