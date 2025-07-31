package com.thred.datingapp.common.entity.report;

import com.thred.datingapp.common.entity.BaseEntity;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.field.UserState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToOne
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private String evidenceUrl;

    @Builder
    public Report(ReportType reportType, String content, ReportReason reason, User reporter, User reportedUser, Long targetId,
                  String evidenceUrl) {
        this.reportType = reportType;
        this.content = content;
        this.reason = reason;
        this.reporter = reporter;
        this.reportedUser = reportedUser;
        this.targetId = targetId;
        this.evidenceUrl = evidenceUrl;
    }

}
