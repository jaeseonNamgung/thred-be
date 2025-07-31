package com.thred.datingapp.common.entity.inApp.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RevocationStatus {
    IN_PROGRESS("진행 중"),
    FAILED("환불 실패"),
    SUCCESS("환불 성공");

    private final String description;
}
