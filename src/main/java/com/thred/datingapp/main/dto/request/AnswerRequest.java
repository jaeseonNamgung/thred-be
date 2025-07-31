package com.thred.datingapp.main.dto.request;

import com.thred.datingapp.common.entity.Answer;
import com.thred.datingapp.common.entity.user.Question;
import com.thred.datingapp.common.entity.user.User;
import jakarta.validation.constraints.NotEmpty;

public record AnswerRequest(
        @NotEmpty(message = "답변은 필수입니다.")
        String answer1,
        @NotEmpty(message = "답변은 필수입니다.")
        String answer2,
        @NotEmpty(message = "답변은 필수입니다.")
        String answer3
) {
    public static Answer toEntity(AnswerRequest request, User sender, User receiver, Question question) {
        return Answer.builder()
                .answer1(request.answer1())
                .answer2(request.answer2())
                .answer3(request.answer3())
                .sender(sender)
                .receiver(receiver)
                .question(question)
                .build();
    }
}
