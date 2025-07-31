package com.thred.datingapp.report.repository.queryDsl;

import com.thred.datingapp.common.entity.report.Report;
import com.thred.datingapp.common.entity.report.ReportType;
import org.springframework.data.domain.Page;

public interface ReportQueryDsl {

  Page<Report> findAllByReportTypeFetchReportedUserWithPaging(ReportType reportType, Long pageLastId, int pageSize);
}
