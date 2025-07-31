package com.thred.datingapp.common.entity.inApp.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum ConsumptionState {

    NOT_CONSUMED(0), // 소비되지 않음
    CONSUMED(1); // 소비됨

    private final int code;

    private static final Map<Integer, ConsumptionState> consumptionStateMap =
            Stream.of(values()).collect(Collectors.toUnmodifiableMap(ConsumptionState::getCode, Function.identity()));

    public static ConsumptionState findType(int code){
        return consumptionStateMap.get(code);
    }

}
