package com.thred.datingapp.common.entity.report;

import com.thred.datingapp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
public class ReportHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long reportedUserId;
    @Column(nullable = false)
    private String reportedUserName;
    @Column(nullable = false)
    private String reportedUserEmail;
    @Column(nullable = false)
    private Long targetId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportResult result;

    @Column
    private LocalDate suspendedDate;


    @Builder
    public ReportHistory(Long reportedUserId, String reportedUserName, String reportedUserEmail, Long targetId, ReportReason reason, ReportType reportType) {
        this.reportedUserId = reportedUserId;
        this.reportedUserName = reportedUserName;
        this.reportedUserEmail = reportedUserEmail;
        this.targetId = targetId;
        this.reason = reason;
        this.result = ReportResult.REVIEW_PENDING;
        this.reportType = reportType;
        this.suspendedDate = LocalDate.now();
    }

}
