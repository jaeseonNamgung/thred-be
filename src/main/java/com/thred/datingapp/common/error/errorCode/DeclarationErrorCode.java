package com.thred.datingapp.common.error.errorCode;

import com.thred.datingapp.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DeclarationErrorCode implements ErrorCode {
    NO_CHAT_ROOM(HttpStatus.BAD_REQUEST,"유요하지 않은 채팅방 id 입니다."),
    ALREADY_DECLARATION(HttpStatus.BAD_REQUEST,"이미 신고를 하셨습니다"),
    INVALID_DECLARATION_REQUEST(HttpStatus.BAD_REQUEST,"로그인된 회원만 신고 가능합니다");

    private final HttpStatus httpStatus;
    private final String message;
}
