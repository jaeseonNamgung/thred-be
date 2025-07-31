package com.thred.datingapp.admin.repository.queryDsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thred.datingapp.common.entity.admin.QReview;
import com.thred.datingapp.common.entity.admin.Review;
import com.thred.datingapp.common.entity.admin.ReviewStatus;
import com.thred.datingapp.common.entity.community.QCommunity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.thred.datingapp.common.entity.admin.QReview.review;

@RequiredArgsConstructor
public class ReviewQueryDslImpl implements ReviewQueryDsl{

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Review> findByReviewStatusFetchUserOrderByCreatedDateDescWithPaging(ReviewStatus reviewStatus, Long pageLastId, int pageSize) {
    List<Review> content = queryFactory.selectFrom(review)
                                     .join(review.user)
                                     .fetchJoin()
                                     .where(review.reviewStatus.eq(reviewStatus), pageLastIdCondition(pageLastId))
                                     .limit(pageSize)
                                     .orderBy(review.createdDate.desc())
                                     .fetch();
    JPAQuery<Long> countQuery = queryFactory.select(review.count()).from(review).where(review.reviewStatus.eq(reviewStatus));
    return PageableExecutionUtils.getPage(content, Pageable.ofSize(pageSize), countQuery::fetchOne);
  }

  private BooleanExpression pageLastIdCondition(Long pageLastId) {
    return pageLastId != null && pageLastId > 0 ? review.id.lt(pageLastId) : null;
  }
}
