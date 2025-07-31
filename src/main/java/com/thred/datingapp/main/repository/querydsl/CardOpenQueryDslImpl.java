package com.thred.datingapp.main.repository.querydsl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thred.datingapp.common.entity.card.QCard;
import com.thred.datingapp.common.entity.community.QCommunity;
import com.thred.datingapp.main.dto.response.CardOpenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.thred.datingapp.common.entity.card.QCard.card;
import static com.thred.datingapp.common.entity.card.QCardOpen.cardOpen;
import static com.thred.datingapp.common.entity.community.QCommunity.community;
import static com.thred.datingapp.common.entity.user.QUser.user;

@RequiredArgsConstructor
public class CardOpenQueryDslImpl implements CardOpenQueryDsl {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<CardOpenResponse> findCardOpenResponseByOpenerIdWithPaging(Long openerId, long pageLastId, int pageSize) {

    List<CardOpenResponse> contents = queryFactory.select(
                                                      Projections.constructor(CardOpenResponse.class, cardOpen.card.id.as("cardId"), cardOpen.card.profileUser.id.as("profileUserId"),
                                                                              cardOpen.card.profileUser.mainProfile.as("mainProfile")))
                                                  .from(cardOpen)
                                                  .leftJoin(cardOpen.card, card)
                                                  .leftJoin(card.profileUser, user)
                                                  .where(cardOpen.opener.id.eq(openerId), pageLastIdCondition(pageLastId))
                                                  .limit(pageSize)
                                                  .orderBy(cardOpen.createdDate.desc())
                                                  .fetch();
    JPAQuery<Long> countQuery = queryFactory.select(cardOpen.count())
                                            .from(cardOpen)
                                            .leftJoin(cardOpen.card, card)
                                            .leftJoin(card.profileUser, user)
                                            .where(cardOpen.opener.id.eq(openerId));
    return PageableExecutionUtils.getPage(contents, Pageable.ofSize(pageSize), countQuery::fetchOne);
  }

  @Override
  public boolean existsByOpenerIdAndCardId(Long openerId, Long cardId) {
    Integer result = queryFactory.selectOne().from(cardOpen).where(cardOpen.opener.id.eq(openerId), cardOpen.card.id.eq(cardId)).fetchFirst();
    return result != null;
  }

  private BooleanExpression pageLastIdCondition(Long pageLastId) {
    QCommunity community = QCommunity.community;
    return pageLastId != null && pageLastId > 0 ? community.id.lt(pageLastId) : null;
  }
}
