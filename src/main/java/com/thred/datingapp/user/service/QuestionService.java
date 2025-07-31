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

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class QuestionService {

  private final QuestionRepository questionRepository;

  public Question findQuestionByUserId(Long userId) {
    return questionRepository.findByUserId(userId)
                             .orElseThrow(() -> {
                               log.error("[findQuestionByUserId] 존재하지 않은 질문 정보입니다. ===> userId: {}", userId);
                               return new CustomException(ProfileErrorCode.QUESTION_NOT_FOUND);
                             });
  }
}
