package com.thred.datingapp.user.api.response;

public record ProcessingResultResponse(
        boolean status
) {
    public static ProcessingResultResponse from(boolean status) {
        return new ProcessingResultResponse(status);
    }
}
