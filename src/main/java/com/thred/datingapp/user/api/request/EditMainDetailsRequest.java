package com.thred.datingapp.user.api.request;

import com.thred.datingapp.main.dto.request.EditDetailsRequest;
import com.thred.datingapp.main.dto.request.EditUserRequest;
import jakarta.validation.Valid;

public record EditMainDetailsRequest(
        boolean questionChange,
        boolean introduceChange,
        @Valid EditUserRequest user,
        @Valid EditDetailsRequest details
) {
}
