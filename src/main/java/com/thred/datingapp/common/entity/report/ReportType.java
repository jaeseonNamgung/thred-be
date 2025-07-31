package com.thred.datingapp.common.entity.report;

import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.ReportErrorCode;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Getter
public enum ReportType {
    COMMENT("COMMENT"),
    COMMUNITY("COMMUNITY"),
    CHAT("CHAT"),
    COMPLETE("COMPLETE"),
    ;

    private final String type;
    private static final Map<String, ReportType> descriptions =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(ReportType::name, Function.identity())));


    public static ReportType findType(String result) {
        String upperResult = result.toUpperCase();
        ReportType reportType = descriptions.get(upperResult);
        if (reportType == null) {
            log.error("[findType] 알수 없는 ReportType ===> type: {}", result);
            throw new CustomException(ReportErrorCode.INVALID_REPORT_TYPE);
        }
        return reportType;
    }
}
