package com.thred.datingapp.common.error.errorCode;

import com.thred.datingapp.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
@Getter
@RequiredArgsConstructor
public enum FcmTokenErrorCode implements ErrorCode {

    FCM_RESPONSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 응답 오류입니다."),
    MAKE_MESSAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "메세지 생성 중 오류입니다."),
    GET_ACCESS_TOKEN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 생성 중 오류입니다.");
    private final HttpStatus httpStatus;
    private final String message;
}
