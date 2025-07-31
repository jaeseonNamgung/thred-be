package com.thred.datingapp.common.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    HttpStatus getHttpStatus();
    String getMessage();
}
