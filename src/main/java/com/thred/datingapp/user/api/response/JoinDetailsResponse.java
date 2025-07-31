package com.thred.datingapp.user.api.response;

import com.thred.datingapp.common.entity.user.UserDetail;

public record JoinDetailsResponse(
        Integer height,
        String drink,
        String smoke,
        Integer temperature,
        String belief,
        String oppositeFriends,
        String job,
        String mbti
) {
    public static JoinDetailsResponse of(UserDetail userDetail) {
        return new  JoinDetailsResponse(
                userDetail.getHeight(),
                userDetail.getDrink().getDrink(),
                userDetail.getSmoke().getSmoke(),
                userDetail.getTemperature(),
                userDetail.getBelief() != null ? userDetail.getBelief().getBelief() : null,
                userDetail.getOppositeFriends()!=null? userDetail.getOppositeFriends().getAmount() : null,
                userDetail.getJob() != null ? userDetail.getJob().getJob() : null,
                userDetail.getMbti() != null ? userDetail.getMbti().getMbti() : null
        );
    }
}
