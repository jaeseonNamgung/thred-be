package com.thred.datingapp.main.dto.response;

import com.thred.datingapp.common.entity.user.Question;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.main.dto.request.EditTotalRequest;
import com.thred.datingapp.main.dto.request.EditUserRequest;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public record UserResponse(
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
    public static UserResponse of(User user, Question question) {
        String city = user.getAddress().getCity();
        String province = user.getAddress().getProvince();
        return new UserResponse(user.getId(), user.getUsername(), city, province, user.getAge(), user.getIntroduce(),
                question.getQuestion1(), question.getQuestion2(), question.getQuestion3());
    }

    public static UserResponse of(EditTotalRequest request, User user) {
        EditUserRequest editUser = request.user();
        String city = user.getAddress().getCity();
        String province = user.getAddress().getProvince();
        return new UserResponse(user.getId(), user.getUsername(), city, province, user.getAge(), editUser.introduce(),
                editUser.question1(), editUser.question2(), editUser.question3());
    }

    public static UserResponse of(EditTotalRequest request, User user, String introduce) {
        EditUserRequest editUser = request.user();
        String city = user.getAddress().getCity();
        String province = user.getAddress().getProvince();
        return new UserResponse(user.getId(), user.getUsername(), city, province,user.getAge(), introduce,
                editUser.question1(), editUser.question2(), editUser.question3());
    }

    public static UserResponse of(EditTotalRequest request, User user, Question question) {
        EditUserRequest editUser = request.user();
        String city = user.getAddress().getCity();
        String province = user.getAddress().getProvince();
        return new UserResponse(user.getId(), user.getUsername(), city, province,user.getAge(), editUser.introduce(),
                question.getQuestion1(), question.getQuestion2(), question.getQuestion3());
    }
}
