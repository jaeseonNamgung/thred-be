package com.thred.datingapp.main.dto.request;

import com.thred.datingapp.common.entity.user.Question;
import com.thred.datingapp.common.entity.user.User;
import jakarta.validation.constraints.NotEmpty;

public record EditUserRequest(
        @NotEmpty(message = "자기소개는 필수입니다.")
        String introduce,
        @NotEmpty(message = "질문은 필수입니다.")
        String question1,
        @NotEmpty(message = "질문은 필수입니다.")
        String question2,
        @NotEmpty(message = "질문은 필수입니다.")
        String question3
) {
    public static Question toQuestionEntity(EditUserRequest request, User user) {
        return Question.builder()
                .question1(request.question1)
                .question2(request.question2)
                .question3(request.question3)
                .user(user)
                .build();
    }
}
