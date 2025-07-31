package com.thred.datingapp.common.entity.inApp.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum RevocationReason {

    REFUNDED_FOR_OTHER_REASON(0, "기타(알 수 없는) 사유로 인한 환불"),
    REFUNDED_DUE_TO_ISSUE(1, "구매자 변심으로 인한 환불"),
    NOT_RECEIVED(2, "상품 미수령으로 인한 환불"),
    DEFECTIVE(3, "결함으로 인한 환불"),
    ACCIDENTAL(4, "구매자 실수로 인한 환불"),
    FRAUD(5, "사기 의심으로 인한 환불"),
    UNACKNOWLEDGED(7, "인지하지 못한 구매로 인한 환불");

    private final int value;
    private final String description;

    private static final Map<Integer, RevocationReason> descriptions =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(RevocationReason::getValue, Function.identity())));

    public static RevocationReason findType(int value) {
        return Optional.ofNullable(descriptions.get(value)).orElse(REFUNDED_FOR_OTHER_REASON);
    }

}
