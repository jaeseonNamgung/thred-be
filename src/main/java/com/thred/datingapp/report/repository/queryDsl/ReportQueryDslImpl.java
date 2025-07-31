package com.thred.datingapp.report.repository.queryDsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thred.datingapp.common.entity.report.Report;
import com.thred.datingapp.common.entity.report.ReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.thred.datingapp.common.entity.report.QReport.report;

@RequiredArgsConstructor
public class ReportQueryDslImpl implements ReportQueryDsl{

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Report> findAllByReportTypeFetchReportedUserWithPaging(ReportType reportType, Long pageLastId, int pageSize) {

    List<Report> content = queryFactory.selectFrom(report)
                                     .join(report.reportedUser)
                                     .where(report.reportType.eq(reportType), pageLastIdCondition(pageLastId))
                                     .limit(pageSize)
                                     .orderBy(report.createdDate.desc())
                                     .fetch();
    JPAQuery<Long> countQuery = queryFactory.select(report.count()).from(report).where(report.reportType.eq(reportType));
    return PageableExecutionUtils.getPage(content, Pageable.ofSize(pageSize), countQuery::fetchOne);
  }
  private BooleanExpression pageLastIdCondition(Long pageLastId) {
    return pageLastId != null && pageLastId > 0 ? report.id.lt(pageLastId) : null;
  }
}
