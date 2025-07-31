package com.thred.datingapp.common.entity.inApp.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum PurchaseState {
    PURCHASED(0), // 구매 완료
    CANCELED(1), // 구매 취소
    REFUNDED(2); // 환불

    private final int code;

    private static final Map<Integer, PurchaseState> purchaseStateMap =
            Stream.of(values()).collect(Collectors.toUnmodifiableMap(PurchaseState::getCode, Function.identity()));

    public static PurchaseState findType(int code) {
        return purchaseStateMap.get(code);
    }
}
