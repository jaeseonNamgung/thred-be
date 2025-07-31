package com.thred.datingapp.common.error.errorCode;

import com.thred.datingapp.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum InAppErrorCode implements ErrorCode {

    PURCHASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "결제 중 오류입니다."),
    APPLE_CA_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "애플 CA 인증 오류입니다."),
    SIGNATURE_VERIFICATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서명 검증 실패입니다."),

    NOT_EXIST_THREAD(HttpStatus.NOT_FOUND, "실이 부족합니다."),
    INSUFFICIENT_THREAD_COUNT(HttpStatus.BAD_REQUEST, "실타래 수량이 부족합니다."),
    NOT_EXIST_PRODUCT(HttpStatus.NOT_FOUND, "존재하지 않은 상품입니다."),
    RECEIPT_NOT_VALID(HttpStatus.INTERNAL_SERVER_ERROR,"유효하지 않은 영수증입니다."),
    APP_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "인앱 서버 에러입니다."),
    RECEIPT_SIGNATURE_VERIFICATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "영수증 서명 검증을 실패했습니다."),
    NOT_EXIST_RECEIPT(HttpStatus.INTERNAL_SERVER_ERROR,"존재하지 않은 영수증입니다."),
    NOT_EXIST_USER_ASSET(HttpStatus.INTERNAL_SERVER_ERROR, "회원 실 사용 이력이 존재하지 않습니다."),
    NOT_EXIST_PURCHASE_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "구매 타입이 존재하지 않습니다."),
    GOOGLE_PLAY_AUTH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Google Play API 인증 실패입니다."),
    GOOGLE_PLAY_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Google Play API 에러입니다."),
    GOOGLE_CREDENTIALS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Google 인증 정보를 불러오는 중 오류가 발생했습니다. 서버 설정을 확인하세요."),
    GOOGLE_CREDENTIALS_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"Google 인증 정보를 처리하는 중 예기치 않은 오류가 발생했습니다. 서버 설정 및 네트워크 상태를 확인하세요."),
    GOOGLE_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Google Play 구독 정보를 확인하는 중 네트워크 또는 입출력 오류가 발생했습니다."),
    GOOGLE_NOTIFICATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "구글 인앱 알림 처리 중 오류가 발생했습니다."),
    ALREADY_PURCHASED_PRODUCT(HttpStatus.BAD_REQUEST,"이미 구매한 상품입니다.");




    private final HttpStatus httpStatus;
    private final String message;
}
