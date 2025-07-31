package com.thred.datingapp.report.repository.queryDsl;

import com.thred.datingapp.common.entity.report.ReportHistory;
import org.springframework.data.domain.Page;

public interface ReportHistoryQueryDsl {

  Page<ReportHistory> findAllWithPaging(Long pageLastId, int pageSize);
}
