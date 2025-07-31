package com.thred.datingapp.report.dto.request;

import com.thred.datingapp.common.entity.report.ReportReason;
import com.thred.datingapp.common.entity.report.ReportType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ReportRequest(
        // 신고 대상 사용자 아이디
        @NotNull Long reportedUserId,
        @NotEmpty String reportType,
        @NotEmpty String reportReason,
        @NotEmpty String content,
        @NotNull Long targetId
) {

    public ReportType getReportType() {
        return ReportType.findType(reportType);
    }

    public ReportReason getReportReason() {
        return ReportReason.findReason(reportReason);
    }
}
