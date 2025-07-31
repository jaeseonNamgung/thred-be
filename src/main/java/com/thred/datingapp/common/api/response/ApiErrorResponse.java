package com.thred.datingapp.common.api.response;

import com.thred.datingapp.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiErrorResponse{

    private final Boolean status;
    private final Integer httpStatus;
    private final String errorType;
    private final String message;

    public static ApiErrorResponse of(Boolean status, Integer httpStatus, String errorType, String message) {
        return new ApiErrorResponse(
                status,
                httpStatus,
                errorType,
                message
        );
    }

    public static ApiErrorResponse of(Boolean status, ErrorCode errorCode) {
        return new ApiErrorResponse(
                status,
                errorCode.getHttpStatus().value(),
                errorCode.getHttpStatus().name(),
                errorCode.getMessage()
        );
    }
}
