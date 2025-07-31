package com.thred.datingapp.user.api.request;


import jakarta.validation.constraints.NotBlank;

public record BlockInfoRequest(
        @NotBlank(message = "차단 이름은 필수 입니다.")
        String name,
        @NotBlank(message = "차단 전화번호는 필수 입니다.")
        String number
) {
}
