package com.thred.datingapp.main.dto.response;

import com.thred.datingapp.common.entity.user.UserDetail;
import com.thred.datingapp.main.dto.request.EditDetailsRequest;
import com.thred.datingapp.main.dto.request.EditTotalRequest;

public record DetailsResponse(
        Integer height,
        String drink,
        String smoke,
        Integer temperature,
        String belief,
        String oppositeFriends,
        String job,
        String mbti
) {
    public static DetailsResponse of(UserDetail userDetail) {
        return new DetailsResponse(
                userDetail.getHeight(),
                userDetail.getDrink().getDrink(),
                userDetail.getSmoke().getSmoke(),
                userDetail.getTemperature(),
                userDetail.getBelief() != null ? userDetail.getBelief().getBelief() : null,
                userDetail.getOppositeFriends() != null ? userDetail.getOppositeFriends().getAmount() : null,
                userDetail.getJob() != null ? userDetail.getJob().getJob() : null,
                userDetail.getMbti() != null ? userDetail.getMbti().getMbti() : null
        );
    }

    public static DetailsResponse of(EditTotalRequest request) {
        EditDetailsRequest details = request.details();
        return new DetailsResponse(
                details.height(),
                details.drink(),
                details.smoke(),
                details.temperature(),
                details.belief(),
                details.oppositeFriends(),
                details.job(),
                details.mbti()
        );
    }
}
