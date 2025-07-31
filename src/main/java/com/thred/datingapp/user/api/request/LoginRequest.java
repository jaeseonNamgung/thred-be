package com.thred.datingapp.user.api.request;

import jakarta.validation.constraints.NotEmpty;

public record LoginRequest(
        @NotEmpty String username
        ) {
}
