package com.thred.datingapp.common.error.errorCode;

import com.thred.datingapp.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReportErrorCode implements ErrorCode {
    NO_CHAT_ROOM(HttpStatus.BAD_REQUEST, "유요하지 않은 채팅방 id 입니다."),
    ALREADY_REPORT(HttpStatus.BAD_REQUEST, "이미 신고를 하셨습니다"),
    INVALID_REPORT_REQUEST(HttpStatus.BAD_REQUEST, "회원 아이디가 올바르지 않습니다."),
    NO_REPORT(HttpStatus.INTERNAL_SERVER_ERROR, "사용자로 부터 요청된 신고가 존재하지 않습니다."),
    NO_REPORT_HISTORY(HttpStatus.INTERNAL_SERVER_ERROR, "신고 이력이 존재하지 않습니다."),
    INVALID_REPORT_TYPE(HttpStatus.BAD_REQUEST, "신고 타입이 올바르지 않습니다."),
    INVALID_REPORT_REASON(HttpStatus.BAD_REQUEST, "신고 이유가 올바르지 않습니다."),
    INVALID_REPORT_ID(HttpStatus.BAD_REQUEST, "존재하지 않은 신고입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
