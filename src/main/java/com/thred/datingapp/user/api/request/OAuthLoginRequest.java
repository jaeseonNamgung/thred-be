package com.thred.datingapp.user.api.request;

import com.thred.datingapp.common.entity.user.field.LoginType;

public record OAuthLoginRequest(
    String accessToken,
    // 현재 카카오 로그인으로만 구현되어 있어 사용안하지만, 추후 소셜 로그인 추가 시 분기 처리를 위해 사용
    LoginType loginType) {
}
