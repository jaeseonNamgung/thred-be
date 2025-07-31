package com.thred.datingapp.user.api.request;

import com.thred.datingapp.common.entity.user.Question;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.field.Address;
import com.thred.datingapp.common.entity.user.field.Gender;
import com.thred.datingapp.common.entity.user.field.PartnerGender;
import com.thred.datingapp.common.entity.user.field.Role;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record RejoinUserRequest(
        @NotEmpty(message = "이메일은 필수입니다.")
        String email,
        @NotEmpty(message = "성별은 필수입니다.")
        String gender,
        @NotEmpty(message = "닉네임은 필수입니다.")
        String username,
        @NotEmpty(message = "거주하고 있는 도시 정보는 필수입니다.")
        String city,
        @NotEmpty(message = "거주하고 있는 도시 정보는 필수입니다.")
        String province,
        @NotEmpty(message = "생일 정보는 필수입니다.")
        String birth,
        @NotEmpty(message = "자기소개는 필수입니다.")
        String introduce,
        @NotEmpty(message = "전화번호는 필수입니다.")
        String number,
        @NotEmpty(message = "질문은 필수입니다.")
        String question1,
        @NotEmpty(message = "질문은 필수입니다.")
        String question2,
        @NotEmpty(message = "질문은 필수입니다.")
        String question3,
        String code,
        @NotEmpty(message = "상대 성별 정보는 필수입니다.")
        String partnerGender
) {
    public static User fromEntity(RejoinUserRequest request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birthLocalDate = LocalDate.parse(request.birth(), formatter);
        return User.builder()
                .role(Role.USER)
                .username(request.username())
                .birth(birthLocalDate)
                .gender(Gender.findGender(request.gender()))
                .email(request.email())
                .address(Address.of(request.city(), request.province()))
                .introduce(request.introduce())
                .partnerGender(PartnerGender.findGender(request.partnerGender()))
                .phoneNumber(request.number())
                .certification(null)
                .inputCode(request.code())
                .build();
    }

    public static Question fromEntity(RejoinUserRequest request, User user) {
        return Question.builder()
                .question1(request.question1)
                .question2(request.question2)
                .question3(request.question3)
                .user(user)
                .build();
    }
}
