package com.thred.datingapp.report.repository.queryDsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thred.datingapp.common.entity.report.QReportHistory;
import com.thred.datingapp.common.entity.report.ReportHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.thred.datingapp.common.entity.admin.QReview.review;
import static com.thred.datingapp.common.entity.report.QReportHistory.reportHistory;

@RequiredArgsConstructor
public class ReportHistoryQueryDslImpl implements ReportHistoryQueryDsl{

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Page<ReportHistory> findAllWithPaging(Long pageLastId, int pageSize) {
    List<ReportHistory> content = jpaQueryFactory.selectFrom(reportHistory)
                                               .where(pageLastIdCondition(pageLastId))
                                               .limit(pageSize)
                                               .orderBy(reportHistory.createdDate.desc())
                                               .fetch();
    JPAQuery<Long> countQuery = jpaQueryFactory.select(reportHistory.count()).from(reportHistory);
    return PageableExecutionUtils.getPage(content, Pageable.ofSize(pageSize), countQuery::fetchOne);
  }

  private BooleanExpression pageLastIdCondition(Long pageLastId) {
    return pageLastId != null && pageLastId > 0 ? reportHistory.id.lt(pageLastId) : null;
  }
}
