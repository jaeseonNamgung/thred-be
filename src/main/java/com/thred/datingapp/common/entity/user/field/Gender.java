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
public enum Gender {
    MALE("남성"),
    FEMALE("여성");

    private static final Map<String, Gender> descriptions =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(Gender::getGender, Function.identity())));
    private final String gender;

    public static Gender findGender(String input) {
        Gender findGender = descriptions.get(input);
        if (findGender == null) {
            throw new CustomException(UserErrorCode.INVALID_GENDER_STATUS);
        }
        return findGender;
    }
}
