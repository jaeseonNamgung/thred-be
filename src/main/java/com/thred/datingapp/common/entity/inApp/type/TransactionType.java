package com.thred.datingapp.common.entity.inApp.type;

import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.InAppErrorCode;
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
public enum TransactionType {
        PURCHASE("PURCHASE", "소모성"),
        RENEWAL("RENEWAL", "구독");

        private final String name;
        private final String description;

        private static final Map<String, TransactionType> descriptions =
                Collections.unmodifiableMap(Stream.of(values())
                        .collect(Collectors.toMap(TransactionType::getName, Function.identity())));

        public static TransactionType findType(String name) {
            return Optional.ofNullable(descriptions.get(name.toUpperCase()))
                    .orElseThrow(()->{
                        log.error("[findType] 존재하지 않은 타입입니다.(Not exist type) ===> TransactionType: {}", name);
                        return new CustomException(InAppErrorCode.APP_SERVER_ERROR);
                    });
        }


}
