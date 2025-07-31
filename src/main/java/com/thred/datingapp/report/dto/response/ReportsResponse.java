package com.thred.datingapp.report.dto.response;

import com.thred.datingapp.common.entity.report.Report;
import com.thred.datingapp.common.entity.report.ReportHistory;

import java.util.List;

public record ReportsResponse(
        List<ReportResponse> reportResponses
) {
    public static ReportsResponse fromReports(List<Report> reports) {
        List<ReportResponse> reportList = reports.stream()
                .map(report -> new ReportResponse(
                        report.getId(),
                        report.getReportedUser().getId(),
                        report.getTargetId(),
                        report.getReportType().getType(),
                        report.getReason().getReason()))
                .toList();
        return new ReportsResponse(reportList);
    }
    public static ReportsResponse fromHistories(List<ReportHistory> histories) {
        List<ReportResponse> reportList = histories.stream()
                .map(reportHistory -> new ReportResponse(
                        reportHistory.getId(),
                        reportHistory.getReportedUserId(),
                        reportHistory.getTargetId(),
                        reportHistory.getReportType().getType(),
                        reportHistory.getReason().getReason()))
                .toList();
        return new ReportsResponse(reportList);
    }
}
