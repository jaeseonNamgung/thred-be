package com.thred.datingapp.common.entity.user.field;

import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Smoke {
    NONE("비흡연"),
    SMOKER("흡연"),
    ELECTRONIC_CIGARETTE("전자담배"),
    NO_SMOKE("담배 끊는 중");

    private static final Map<String, Smoke> descriptions = Collections.unmodifiableMap(
            Stream.of(values()).collect(Collectors.toMap(Smoke::getSmoke, Function.identity())));
    private final String smoke;

    public static Smoke findSmoke(String input) {
        Smoke smoke = descriptions.get(input);
        if (smoke == null) {
            throw new CustomException(UserErrorCode.INVALID_SMOKING_STATUS);
        }
        return smoke;
    }
}
