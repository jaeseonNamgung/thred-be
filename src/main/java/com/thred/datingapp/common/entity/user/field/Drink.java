package com.thred.datingapp.common.entity.user.field;

import static com.thred.datingapp.common.error.errorCode.UserErrorCode.INVALID_DRINKING_STATUS;

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
public enum Drink {

    NO("안 마셔요"),
    REQUIRED("어쩔수 없을때만"),
    SOMETIMES("가끔 마셔요"),
    ENJOY("조금 즐겨요"),
    LIKE("좋아해요"),
    DRINKER("애주가");

    private static final Map<String, Drink> descriptions =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(Drink::getDrink, Function.identity())));
    private final String drink;

    public static Drink findDrink(String input) {
        Drink findDrink = descriptions.get(input);
        if (findDrink == null) {
            throw new CustomException(INVALID_DRINKING_STATUS);
        }
        return findDrink;
    }

}
