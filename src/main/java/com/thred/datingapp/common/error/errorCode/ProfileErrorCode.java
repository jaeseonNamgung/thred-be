package com.thred.datingapp.common.error.errorCode;

import static org.springframework.http.HttpStatus.*;

import com.thred.datingapp.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProfileErrorCode implements ErrorCode {
    CANT_SAVE_IN_S3(INTERNAL_SERVER_ERROR,"S3 사진 저장에 실패했습니다."),
    QUESTION_NOT_FOUND(INTERNAL_SERVER_ERROR,"존재하지 않은 질문 정보입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
