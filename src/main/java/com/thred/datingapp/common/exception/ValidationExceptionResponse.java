package com.thred.datingapp.common.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.validation.BindingResult;

import java.util.List;

public record ValidationExceptionResponse(
        boolean status,
        int httpStatus,
        List<FieldErrorResponse> fieldError

) {
    public static ValidationExceptionResponse of(HttpStatusCode httpStatus, BindingResult bindingResult) {
        return new ValidationExceptionResponse(
                false ,
                httpStatus.value(),
                FieldErrorResponse.create(bindingResult));
    }
}
