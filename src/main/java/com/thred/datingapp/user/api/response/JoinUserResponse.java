package com.thred.datingapp.user.api.response;

import com.thred.datingapp.common.entity.user.Question;
import com.thred.datingapp.common.entity.user.User;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record JoinUserResponse(
        @NotEmpty Long userId,
        @NotEmpty String email,
        @NotEmpty String gender,
        @NotEmpty String username,
        @NotEmpty String city,
        @NotEmpty String province,
        @NotEmpty String birth,
        @NotEmpty String introduce,
        @NotEmpty String number,
        @NotEmpty String question1,
        @NotEmpty String question2,
        @NotEmpty String question3,
        @NotEmpty String partnerGender,
        Integer thread,
        String inputCode,
        String userCode
) {
    public static JoinUserResponse of(User user, Question question, int thread) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return new JoinUserResponse(user.getId(), user.getEmail(), user.getGender().getGender(), user.getUsername(),
                user.getAddress().getCity(),
                user.getAddress().getProvince(), user.getBirth().format(formatter), user.getIntroduce(), user.getPhoneNumber(),
                question.getQuestion1(), question.getQuestion2(), question.getQuestion3(),
                user.getPartnerGender().getGender(), thread, user.getInputCode(), user.getCode());
    }
}
