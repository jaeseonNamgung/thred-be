package com.thred.datingapp.common.entity.report;

import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.ReportErrorCode;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public enum ReportReason {

    ABUSE("욕설"), SEXUAL_HARASSMENT("성적 발언"), INAPPROPRIATE_IMAGE("부적절한 사진"),
    CONTACT_SHARING("외부 연락처 공유"), INSULTING_REMARK("비하 발언");

    private final String reason;

    private static final Map<String, ReportReason> descriptions =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(ReportReason::name, Function.identity())));

    ReportReason(String reason) {
        this.reason = reason;
    }

    public static ReportReason findReason(String result) {
        String upperResult = result.toUpperCase();
        ReportReason reportResult = descriptions.get(upperResult);
        if (reportResult == null) {
            log.error("[ReportReason] 알 수 없는 ReportReason ===> type: {}", result);
            throw new CustomException(ReportErrorCode.INVALID_REPORT_REASON);
        }
        return reportResult;
    }
}
