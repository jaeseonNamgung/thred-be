package com.thred.datingapp.common.error.errorCode;

import com.thred.datingapp.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BaseErrorCode implements ErrorCode {

    SUCCESS(HttpStatus.OK, "요청이 성공적으로 처리되었습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "요청 오류입니다."),
    TYPE_MISMATCH(HttpStatus.INTERNAL_SERVER_ERROR, "잘못된 타입입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류입니다. 잠시후 다시 시도 바랍니다. (이 오류가 반복되면 관리자에게 문의 바랍니다.)");

    private final HttpStatus httpStatus;
    private final String message;

}
