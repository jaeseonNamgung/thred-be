package com.thred.datingapp.common.exception;

import com.thred.datingapp.common.api.response.ApiDataResponse;
import com.thred.datingapp.common.api.response.ApiErrorResponse;
import com.thred.datingapp.common.api.response.ApiStatusResponse;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.ErrorCode;
import com.thred.datingapp.common.error.errorCode.BaseErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {


    // @Valid 예외 처리 로직
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {

        log.error("[handleMethodArgumentNotValid]Validation error ===> error: {}", ex.getBindingResult().getAllErrors());
        return handleExceptionInternal(ex, ValidationExceptionResponse.of(status, ex.getBindingResult()), headers,
                status, request);
    }

    @ExceptionHandler
    public ResponseEntity<Object> customExceptionHandler(CustomException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getErrorCode(), request);
    }

    @ExceptionHandler
    public ResponseEntity<Object> exception(Exception ex, WebRequest request) {
        log.error("[exception]Unexpected exception occurred ===> errorMessage: {}", ex.getMessage());
        return handleExceptionInternal(ex, BaseErrorCode.INTERNAL_SERVER_ERROR, request);
    }


    private ResponseEntity<Object> handleExceptionInternal(Exception ex, ErrorCode errorCode, WebRequest request) {
        return handleExceptionInternal(ex, errorCode, errorCode.getHttpStatus(), request);
    }

    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, ValidationExceptionResponse response, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        return super.handleExceptionInternal(ex, response, headers, statusCode, request);
    }

    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, ErrorCode errorCode,
                                                             HttpStatusCode statusCode, WebRequest request) {
        return super.handleExceptionInternal(ex, ApiDataResponse.error(ApiStatusResponse.of(false), errorCode), HttpHeaders.EMPTY, statusCode, request);
    }


}
