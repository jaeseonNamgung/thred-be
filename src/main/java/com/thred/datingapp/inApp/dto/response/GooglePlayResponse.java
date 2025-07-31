package com.thred.datingapp.inApp.dto.response;

import com.google.api.services.androidpublisher.model.SubscriptionPurchaseV2;

public record GooglePlayResponse(
        String kind, // androidpublisher 서비스의 inappPurchase 객체
        String regionCode, // 상품이 승인된 시점에 사용자의 ISO 3166-1 alpha-2 청구서 수신 지역 코드
        String purchaseTimeMillis, // 제품이 구매된 시간
        Integer purchaseState, // 주문의 구매 상태 0:구매함 1: 취소됨 2: 대기 중
        Integer consumptionState, // 인앱 상품의 소비 상태, 0: 아직 소비되지 않음 1: 소비함
        String developerPayload, // 주문의 추가 정보가 포함된 개발자 지정 문자열
        String orderId, // 인앱 상품 구매와 연결된 주문 ID
        Integer purchaseType, // 인앱 상품 구매 유형, 이 필드는 이 구매가 표준 인앱 결제 절차를 사용하여 이루어지지 않은 경우에만 설정됩
        // 0: 테스트 (예: 라이선스 테스트 계정으로 구매됨) 1: 프로모션 (프로모션 코드를 사용하여 구매). Play Points 구매는 포함되지 않음 2: 리워드 (지불하는 대신 동영상 광고 시청)
        Integer acknowledgementState, // 인앱 상품의 확인 상태. 가 0: 확인됨
        String purchaseToken, // 구매 토큰 (없을 수 있음)
        String productId, // 인앱 상품 SKU
        Integer quantity, // 인앱 상품 구매와 관련된 수량, 수량이 없는 경우 1
        String obfuscatedExternalAccountId, // 앱에서 사용자 계정과 고유하게 연결된 ID의 난독화된 버전
        String obfuscatedExternalProfileId, // 앱에서 사용자 프로필과 고유하게 연결된 ID의 난독화된 버전

        Integer refundableQuantity // 환불 대상 수량. 즉, 환불되지 않은 수량. 이 값은 수량 기반 부분 환불 및 전액 환불을 반영.
) {

}
