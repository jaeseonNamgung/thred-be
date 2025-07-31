package com.thred.datingapp.common.error.errorCode;

import com.thred.datingapp.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum BoardErrorCode implements ErrorCode {
    NOT_EXIST_POST(HttpStatus.NOT_FOUND, "존재하지 않은 게시글입니다."),
    NOT_EXIST_COMMENT(HttpStatus.NOT_FOUND,"존재하지 않은 댓글입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
