package com.thred.datingapp.user.api.response;

public record FirstJoinResponse(
        Long userId,
        boolean status
) {
    public static FirstJoinResponse of(Long userId, boolean status) {
        return new FirstJoinResponse(userId, status);
    }
}
