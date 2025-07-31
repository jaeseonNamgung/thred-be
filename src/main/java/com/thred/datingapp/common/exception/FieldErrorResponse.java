package com.thred.datingapp.common.exception;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

public record FieldErrorResponse(String field, String message) {

    public static FieldErrorResponse of(String field, String message) {
        return new FieldErrorResponse(field, message);
    }

    public static List<FieldErrorResponse> create(BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        return fieldErrors.stream().map(fieldError ->
                FieldErrorResponse.of(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()))
                .toList();
    }
}
