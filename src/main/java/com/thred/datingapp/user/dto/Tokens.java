package com.thred.datingapp.user.dto;

public record Tokens(
        String accessToken,
        String refreshToken
) {
}
