package com.thred.datingapp.user.api.request;

import com.thred.datingapp.common.entity.user.UserDetail;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.field.Belief;
import com.thred.datingapp.common.entity.user.field.Drink;
import com.thred.datingapp.common.entity.user.field.Job;
import com.thred.datingapp.common.entity.user.field.Mbti;
import com.thred.datingapp.common.entity.user.field.OppositeFriends;
import com.thred.datingapp.common.entity.user.field.Smoke;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record JoinDetailsRequest(
        @NotNull(message = "키 값은 필수입니다.")
        Integer height,
        @NotEmpty(message = "음주 관련 정보는 필수입니다.")
        String drink,
        @NotEmpty(message = "흡연 관련 정보는 필수입니다.")
        String smoke,
        @NotEmpty(message = "종교 관련 정보는 필수입니다.")
        String belief,
        @NotEmpty(message = "이성 친구 정보는 필수입니다.")
        String oppositeFriends,
        @NotEmpty(message = "직업 관련 정보는 필수입니다.")
        String job,
        @NotEmpty(message = "MBTI 정보는 필수입니다.")
        String mbti,
        @NotNull(message = "연애의 온도 값은 필수입니다.")
        Integer temperature
) {
    public static UserDetail fromEntity(JoinDetailsRequest request, User user) {
        return UserDetail.builder()
                .user(user)
                .height(request.height())
                .drink(Drink.findDrink(request.drink()))
                .belief(Belief.findBelief(request.belief()))
                .smoke(Smoke.findSmoke(request.smoke()))
                .oppositeFriends(OppositeFriends.findOppositeFriends(request.oppositeFriends()))
                .mbti(Mbti.findMbti(request.mbti()))
                .job(Job.findJob(request.job()))
                .temperature(request.temperature()).build();
    }
}
