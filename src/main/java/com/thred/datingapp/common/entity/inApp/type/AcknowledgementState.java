package com.thred.datingapp.common.entity.inApp.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum AcknowledgementState {
    NOT_ACKNOWLEDGED(0), // 미확인
    ACKNOWLEDGED(1); // 확인됨

    private final int code;

    private static final Map<Integer, AcknowledgementState> acknowledgementStateMap =
            Stream.of(values()).collect(Collectors.toUnmodifiableMap(AcknowledgementState::getCode, Function.identity()));

    public static AcknowledgementState findType(int code) {
        return acknowledgementStateMap.get(code);
    }
}
