package com.thred.datingapp.main.dto.response;

import com.thred.datingapp.common.entity.Answer;
import com.thred.datingapp.common.entity.user.Question;

public record AnswerAllResponse(
    String answerTargetUsername,
    boolean hasAnswer,
    AnswerResponse answer1,
    AnswerResponse answer2,
    AnswerResponse answer3) {
  public static AnswerAllResponse of(Question question, Answer answer, String answerTargetUsername) {
    if (answer != null) {
      AnswerResponse answer1 = AnswerResponse.of(question.getQuestion1(), answer.getAnswer1());
      AnswerResponse answer2 = AnswerResponse.of(question.getQuestion2(), answer.getAnswer2());
      AnswerResponse answer3 = AnswerResponse.of(question.getQuestion3(), answer.getAnswer3());
      return new AnswerAllResponse(answerTargetUsername, true, answer1, answer2, answer3);
    }
    return new AnswerAllResponse(
        answerTargetUsername,
        false,
        AnswerResponse.of(question.getQuestion1()),
        AnswerResponse.of(question.getQuestion2()),
        AnswerResponse.of(question.getQuestion3()));
  }
}
