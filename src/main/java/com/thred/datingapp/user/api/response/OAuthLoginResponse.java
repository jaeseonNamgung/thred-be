package com.thred.datingapp.user.api.response;

public record OAuthLoginResponse(
        Long userId,
        boolean status,
        boolean certification,
        String role,
        String accessToken,
        String refreshToken
) {

    /**
     * 로그인 성공: 인증 및 토큰 발급 모두 완료된 상태
     */
    public static OAuthLoginResponse success(
            Long userId,
            String role,
            String accessToken,
            String refreshToken
    ) {
        return new OAuthLoginResponse(userId, true, true, role, accessToken, refreshToken);
    }

    /**
     * 로그인 실패: 사용자 인증 자체 실패 (ex. OAuth 인증 실패)
     */
    public static OAuthLoginResponse failure() {
        return new OAuthLoginResponse(null, false, false, null, null, null);
    }

    /**
     * 인증은 성공했으나, 권한으로 인해 제한된 로그인 상태
     */
    public static OAuthLoginResponse authorizationFailure(Long userId, String role, String accessToken, String refreshToken) {
        return new OAuthLoginResponse(userId, true, false, role, accessToken, refreshToken);
    }
}
