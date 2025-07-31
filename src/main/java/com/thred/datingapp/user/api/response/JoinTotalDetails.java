package com.thred.datingapp.user.api.response;

import com.thred.datingapp.common.entity.user.UserDetail;
import com.thred.datingapp.common.entity.user.Question;
import com.thred.datingapp.common.entity.user.User;
import java.util.List;

public record JoinTotalDetails(
        JoinUserResponse user,
        JoinDetailsResponse details,
        String mainProfile,
        List<ProfileResponse> profiles
) {
    public static JoinTotalDetails of(UserDetail userDetail, List<ProfileResponse> profiles, Question question, int thread) {
        User user = userDetail.getUser();
        return new JoinTotalDetails(JoinUserResponse.of(userDetail.getUser(), question, thread),
                JoinDetailsResponse.of(userDetail), user.getMainProfile(), profiles);
    }
}
