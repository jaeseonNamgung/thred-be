package com.testFixture;

import com.thred.datingapp.common.entity.report.Report;
import com.thred.datingapp.common.entity.report.ReportReason;
import com.thred.datingapp.common.entity.report.ReportType;
import com.thred.datingapp.common.entity.user.User;

public class ReportFixture {

  public static Report createCommunityReport(int i, User reporter, User reportedUser) {
    return Report.builder()
                 .reportType(ReportType.COMMUNITY)
                 .content("report content " + i)
                 .targetId(Long.valueOf(i))
                 .reason(ReportReason.ABUSE)
                 .evidenceUrl("evidenceUrl " + i)
                 .reporter(reporter)
                 .reportedUser(reportedUser)
                 .build();
  }
}
