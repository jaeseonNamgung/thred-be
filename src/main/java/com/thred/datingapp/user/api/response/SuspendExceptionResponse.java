package com.thred.datingapp.user.api.response;

import com.thred.datingapp.common.error.ErrorCode;

public record SuspendExceptionResponse(
        boolean status,
        int httpStatus,
        String errorType,
        String message,
        String suspendTime
) {
    public static SuspendExceptionResponse of(ErrorCode errorCode,String suspendTime) {
        return new SuspendExceptionResponse(
                false,
                errorCode.getHttpStatus().value(),
                errorCode.getHttpStatus().name(),
                errorCode.getMessage(),
                suspendTime

        );
    }
}
