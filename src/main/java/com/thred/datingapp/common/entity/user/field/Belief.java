package com.thred.datingapp.common.entity.user.field;

import static com.thred.datingapp.common.error.errorCode.UserErrorCode.INVALID_RELIGION_STATUS;

import com.thred.datingapp.common.error.CustomException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

//CLEAR
@Getter
@RequiredArgsConstructor
public enum Belief {

    NON_RELIGIOUS("무교"),
    BUDDHISM("불교"),
    ROMAN_CATHOLICISM("천주교"),
    CHRISTIANITY("기독교"),
    WON_BUDDHISM("원불교"),
    ELSE("기타 종교");

    private static final Map<String, Belief> descriptions =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(Belief::getBelief, Function.identity())));
    private final String belief;

    public static Belief findBelief(String input) {
        Belief findBelief = descriptions.get(input);
        if (findBelief == null) {
            throw new CustomException(INVALID_RELIGION_STATUS);
        }
        return findBelief;
    }
}
