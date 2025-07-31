package com.thred.datingapp.report.dto.response;

public record ReportContent(
        // 신고 대상 회원 이름
        Long reportId,
        Long reportedUserId,
        String reportedUserName,
        Long targetId,
        String reason,
        String type,
        String evidenceUrl,
        String content
) {
}
