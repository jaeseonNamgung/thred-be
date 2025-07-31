package com.thred.datingapp.main.dto.request;

import com.thred.datingapp.common.entity.user.UserDetail;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record EditDetailsRequest(
        @NotNull(message = "키 값은 필수입니다.")
        Integer height,
        @NotEmpty(message = "음주 관련 정보는 필수입니다.")
        String drink,
        @NotEmpty(message = "흡연 관련 정보는 필수입니다.")
        String smoke,
        @NotEmpty(message = "종교 관련 정보는 필수입니다.")
        String belief,
        @NotEmpty(message = "직업 관련 정보는 필수입니다.")
        String job,
        @NotEmpty(message = "이성 친구 정보는 필수입니다.")
        String oppositeFriends,
        @NotEmpty(message = "MBTI 정보는 필수입니다.")
        String mbti,
        @NotNull(message = "연애의 온도 값은 필수입니다.")
        Integer temperature
) {
    public static EditDetailsRequest of(UserDetail userDetail) {
        return new EditDetailsRequest(
                userDetail.getHeight(),
                userDetail.getDrink() != null ? userDetail.getDrink().getDrink() : null,
                userDetail.getSmoke() != null ? userDetail.getSmoke().getSmoke() : null,
                userDetail.getBelief() != null ? userDetail.getBelief().getBelief() : null,
                userDetail.getJob() != null ? userDetail.getJob().getJob() : null,
                userDetail.getOppositeFriends() != null ? userDetail.getOppositeFriends().getAmount() : null,
                userDetail.getMbti() != null ? userDetail.getMbti().getMbti() : null,
                userDetail.getTemperature()
        );
    }
}
