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
public enum PartnerGender {
    OTHER("이성"),
    SAME("동성");

    private static final Map<String, PartnerGender> descriptions =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(PartnerGender::getGender, Function.identity())));
    private final String gender;

    public static PartnerGender findGender(String input) {
        PartnerGender partnerGender = descriptions.get(input);
        if (partnerGender == null) {
            throw new CustomException(UserErrorCode.INVALID_PARTNER_GENDER_STATUS);
        }
        return partnerGender;
    }
}
