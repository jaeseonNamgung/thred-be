package com.thred.datingapp.inApp.repository.queryDsl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thred.datingapp.common.entity.inApp.QThreadUseHistory;
import com.thred.datingapp.common.entity.inApp.type.PurchaseType;
import com.thred.datingapp.inApp.dto.response.ThreadUseHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.thred.datingapp.common.entity.inApp.QThreadUseHistory.threadUseHistory;

@RequiredArgsConstructor
public class ThreadUseHistoryQueryDslImpl implements ThreadUseHistoryQueryDsl {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ThreadUseHistoryResponse> findHistoryAllByUserIdWithPaging(Long userId, Long pageLastId, int pageSize) {

        List<ThreadUseHistoryResponse> contents = queryFactory.select(
                        Projections.constructor(ThreadUseHistoryResponse.class,
                                threadUseHistory.id.as("threadUseHistoryId"),
                                threadUseHistory.purchaseType.as("purchaseType"),
                                threadUseHistory.amount.as("amount"),
                                threadUseHistory.createdDate.as("createdDate")
                        )
                ).from(threadUseHistory)
                .where(threadUseHistory.user.id.eq(userId), lastIdCondition(pageLastId))
                .limit(pageSize)
                .orderBy(threadUseHistory.createdDate.desc()).fetch();

        JPAQuery<Long> fetchCount = queryFactory.select(threadUseHistory.count()).from(threadUseHistory).where(threadUseHistory.user.id.eq(userId));

        return PageableExecutionUtils.getPage(contents, Pageable.ofSize(pageSize), fetchCount::fetchOne);
    }

    @Override
    public boolean existsByUserIdAndTargetUserIdAndTargetItemId(Long userId, Long purchaseTargetUserId, PurchaseType purchaseType) {
        return queryFactory.selectFrom(threadUseHistory)
                .where(
                        threadUseHistory.user.id.eq(userId),
                        threadUseHistory.purchaseTargetUserId.eq(purchaseTargetUserId),
                        threadUseHistory.purchaseType.eq(purchaseType)
                ).fetchFirst() != null;
    }

    private BooleanExpression lastIdCondition(Long pageLastId) {
        QThreadUseHistory threadUseHistory = QThreadUseHistory.threadUseHistory;
        return pageLastId != null && pageLastId > 0 ? threadUseHistory.id.lt(pageLastId) : null;
    }

}
