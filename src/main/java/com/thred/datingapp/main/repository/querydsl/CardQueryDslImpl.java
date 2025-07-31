package com.thred.datingapp.main.repository.querydsl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thred.datingapp.common.entity.card.Card;
import com.thred.datingapp.common.entity.user.field.Gender;
import com.thred.datingapp.user.api.response.CardProfileResponse;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import static com.thred.datingapp.common.entity.card.QCard.card;
import static com.thred.datingapp.common.entity.card.QCardOpen.cardOpen;
import static com.thred.datingapp.common.entity.user.QBlock.block;

@RequiredArgsConstructor
public class CardQueryDslImpl implements CardQueryDsl {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<CardProfileResponse> findTodayRandomCardByViewerIdGenderCity(Long viewerId, Gender gender, String city) {

    List<Long> blockedUserIds = blockedUserIdsCondition(viewerId);

    Long totalCardCount = countCardByViewerCondition(viewerId, gender, city, blockedUserIds);

    // 50장 이하 : 30장 제공
    // 50장 이상: 전체 70% 제공
    int limit;
    if (totalCardCount <= 50) {
      limit = 30;
    } else {
      limit = (int) Math.floor(totalCardCount * 0.7);
    }

    return queryFactory.select(Projections.constructor(
        CardProfileResponse.class, card.id.as("cardId"),
        card.profileUser.id.as("userId"),
        card.profileUser.username.as("username"),
        card.profileUser.mainProfile.as("mainProfile"),
        Expressions.numberTemplate(Integer.class, "timestampdiff(YEAR, {0}, curdate())", card.profileUser.birth),
        card.profileUser.userDetail.height,
        card.profileUser.address.province,
        card.profileUser.userDetail.temperature))
                       .from(card)
                       .join(card.profileUser)
                       .join(card.profileUser.userDetail)
                       .leftJoin(card.cardOpens, cardOpen) // 오픈된 카드만 조인
                       .on(cardOpen.opener.id.eq(viewerId))
                       .where(card.profileUser.id.ne(viewerId), // 자신의 카드 제외
                              card.profileUser.id.notIn(blockedUserIds), // 차단된 번호 제외
                              cardOpen.id.isNull(), // 오픈한 카드 제외
                              card.profileUser.gender.eq(gender), card.profileUser.address.city.eq(city),
                              Expressions.booleanTemplate("date({0}) != curdate()", card.createdDate) // 오늘자 생성한 카드 제외
                       )
                       .orderBy(Expressions.numberTemplate(Double.class, "rand()").asc()) // mysql 랜덤 함수
                       .limit(limit)
                       .fetch();
  }

  @Override
  public boolean existsByProfileUserId(Long profileUserId) {
    return queryFactory.selectOne().from(card).where(card.profileUser.id.eq(profileUserId)).fetchFirst() != null;
  }

  private Long countCardByViewerCondition(Long viewerId, Gender gender, String city, List<Long> blockedUserIds) {
    return queryFactory.select(card.id.count())
                       .from(card)
                       .leftJoin(card.cardOpens, cardOpen)
                       .on(cardOpen.opener.id.eq(viewerId))
                       .where(card.profileUser.id.ne(viewerId), card.profileUser.id.notIn(blockedUserIds), cardOpen.id.isNull(),
                              card.profileUser.gender.eq(gender), card.profileUser.address.city.eq(city),
                              Expressions.dateTemplate(LocalDate.class, "cast({0} as date)", card.createdDate).ne(LocalDate.now()))
                       .fetchOne();
  }

  private List<Long> blockedUserIdsCondition(Long viewerId) {
    return queryFactory.select(block.blockedUser.id).from(block).where(block.blocker.id.eq(viewerId)).fetch();
  }
}
