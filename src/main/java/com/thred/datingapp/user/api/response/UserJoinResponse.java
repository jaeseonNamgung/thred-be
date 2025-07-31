package com.thred.datingapp.user.api.response;

import com.thred.datingapp.common.entity.user.Question;
import com.thred.datingapp.common.entity.user.User;

public record UserJoinResponse(
        Long userId,
        String username,
        String city,
        String province,
        int age,
        String introduce,
        String question1,
        String question2,
        String question3
) {
    public static UserJoinResponse of(User user, Question question) {
        int age = user.getAge();
        String city = user.getAddress().getCity();
        String province = user.getAddress().getProvince();
        return new UserJoinResponse(user.getId(), user.getUsername(), city, province, age, user.getIntroduce(),
                question.getQuestion1(), question.getQuestion2(), question.getQuestion3());
    }
}
