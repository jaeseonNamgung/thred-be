package com.thred.datingapp.common.error;

import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class SuspendException extends AuthenticationException {

    private final ErrorCode errorCode;
    private final LocalDateTime localDateTime;

    public SuspendException(ErrorCode errorCode,LocalDateTime localDateTime) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.localDateTime = localDateTime;
    }

}
