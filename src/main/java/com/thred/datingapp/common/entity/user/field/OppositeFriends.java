package com.thred.datingapp.common.entity.user.field;

import com.google.common.base.Functions;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OppositeFriends {
    A_LOT("많이 있어요"),
    A_FEW("꽤 있어요"),
    A_LITTLE_BIT("조금 있어요"),
    NO("아예 없어요");

    private static final Map<String, OppositeFriends> descriptions =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(OppositeFriends::getAmount, Functions.identity())));
    private final String amount;

    public static OppositeFriends findOppositeFriends(String value) {
        OppositeFriends oppositeFriends = descriptions.get(value);
        if (oppositeFriends == null) {
            throw new CustomException(UserErrorCode.INVALID_OPPOSITE_FRIENDS_STATUS);
        }
        return oppositeFriends;
    }
}
