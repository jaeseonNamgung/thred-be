package com.thred.datingapp.common.error.errorCode;

import com.thred.datingapp.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {

    NOT_FOUND_CHATROOM(HttpStatus.NOT_FOUND, "존재하지 않은 채팅방입니다."),
    CHAT_INTERCEPTOR_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "채팅 인터셉터 진행중 오류입니다."),
    SSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SSE 전송 에러입니다."),
    CHAT_CONNECTOR_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "채팅방 연결에 실패했습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final String message;

}
