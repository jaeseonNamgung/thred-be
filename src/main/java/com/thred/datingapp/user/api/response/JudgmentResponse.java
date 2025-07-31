package com.thred.datingapp.user.api.response;

public record JudgmentResponse(
        String result,
        String reason,
        JoinTotalDetails joinDetails
) {
    public static JudgmentResponse of(String result, String reason, JoinTotalDetails joinTotalDetails) {
        return new JudgmentResponse(result, reason, joinTotalDetails);
    }
}
