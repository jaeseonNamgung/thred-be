package com.thred.datingapp.report.dto.response;

public record ReportResponse(
        Long reportId,
        Long reportedUserId,
        Long targetId,
        String type,
        String reason
) {

  public static ReportResponse of(
      Long reportId,
      Long reportedUserId,
      Long targetId,
      String type,
      String reason
  ) {
    return new ReportResponse(reportId, reportedUserId, targetId, type, reason);
  }
}
