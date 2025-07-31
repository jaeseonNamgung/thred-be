package com.thred.datingapp.user.api.request;

import jakarta.validation.constraints.NotEmpty;

public record ChangeAddressRequest(
        @NotEmpty(message = "거주하고 있는 도시 정보는 필수입니다.")
        String city,
        @NotEmpty(message = "거주하고 있는 도시 정보는 필수입니다.")
        String province
) {
}
