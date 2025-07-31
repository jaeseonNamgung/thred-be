package com.thred.datingapp.user.api.request;

import jakarta.validation.constraints.NotBlank;

public record ChangePhoneNumberRequest(
        @NotBlank(message = "수정하기 위해 전화번호는 필수입니다.")
        String number
) {
}
