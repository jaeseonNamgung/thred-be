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


//CLEARw
@Getter
@RequiredArgsConstructor
public enum Mbti {
    ISFJ("ISFJ"),
    ISFP("ISFP"),
    ISTJ("ISTJ"),
    ISTP("ISTP"),
    INFJ("INFJ"),
    INTJ("INTJ"),
    INFP("INFP"),
    INTP("INTP"),
    ESFJ("ESFJ"),
    ESFP("ESFP"),
    ESTJ("ESTJ"),
    ESTP("ESTP"),
    ENFJ("ENFJ"),
    ENTJ("ENTJ"),
    ENFP("ENFP"),
    ENTP("ENTP");

    private static final Map<String, Mbti> descriptions =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(Mbti::getMbti, Function.identity())));
    private final String mbti;

    public static Mbti findMbti(String input) {
        Mbti findMbti = descriptions.get(input);
        if (findMbti == null) {
            throw new CustomException(UserErrorCode.INVALID_MBTI_STATUS);
        }
        return findMbti;
    }
}
