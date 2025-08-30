package com.thred.datingapp.user.repository.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thred.datingapp.common.entity.user.QUser;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.field.Gender;
import com.thred.datingapp.common.entity.user.field.LoginType;
import com.thred.datingapp.common.entity.user.field.Role;
import com.thred.datingapp.common.entity.user.field.Smoke;
import com.thred.datingapp.user.api.request.BlockInfoRequest;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.thred.datingapp.common.entity.card.QCard.card;
import static com.thred.datingapp.common.entity.user.QUser.user;
import static com.thred.datingapp.common.entity.user.QUserDetail.userDetail;

@RequiredArgsConstructor
public class UserQueryDslImpl implements UserQueryDsl {
  private static final List<String> METROPOLITAN_AREA                  = List.of("서울", "인천", "경기");
  private static final int          QUIT_DURATION                      = 30;
  private static final int          MAX_CARD_LIMIT_BEFORE_EXPANSION    = 30;
  private static final int          EXPANSION_THRESHOLD                = 50;
  private static final double       VISIBLE_CARD_RATIO_AFTER_EXPANSION = 0.7;

  private final JPAQueryFactory queryFactory;

  public Optional<User> findMatchBySmokePreference(Long userId, List<String> blocks, Gender gender, List<Long> ids, String city, Smoke smoke) {

    BooleanBuilder condition = buildUserFiltersForSmokingMatch(userId, blocks, gender, ids).and(buildRegionalFilter(city, userDetail.user))
                                                                                           .and(buildSmokingPreferenceFilter(smoke));

    User findUser = queryFactory.select(userDetail.user)
                                .from(userDetail)
                                .where(condition)
                                .orderBy(Expressions.numberTemplate(Double.class, "function('RAND')").asc())
                                .fetchFirst();

    return Optional.ofNullable(findUser);
  }

  @Override
  public List<User> findBlockedUsersByPhoneNumberAndName(List<BlockInfoRequest> blockInfoRequests) {
    BooleanBuilder builder = new BooleanBuilder();
    for (BlockInfoRequest block : blockInfoRequests) {
      builder.or(user.username.eq(block.name()).and(user.phoneNumber.eq(block.number())));
    }

    return queryFactory.selectFrom(user).where(builder).fetch();
  }

  private BooleanBuilder buildUserFiltersForSmokingMatch(Long userId, List<String> blocks, Gender gender, List<Long> ids) {
    return new BooleanBuilder().and(userDetail.user.id.ne(userId))
                               .and(userDetail.user.id.notIn(ids))
                               .and(userDetail.user.phoneNumber.notIn(blocks))
                               .and(userDetail.user.gender.eq(gender))
                               .and(userDetail.user.certification.isTrue())
                               .and(userDetail.user.role.eq(Role.USER));
  }

  private BooleanExpression miniProfileCondition(QUser user,
                                                 Long userId,
                                                 List<Long> excludeUserIds,
                                                 String city,
                                                 LocalDate today,
                                                 List<String> blocks,
                                                 Gender gender) {
    return isCreatedToday(user, today).not()
                                      .and(user.id.notIn(excludeUserIds))
                                      .and(user.id.ne(userId))
                                      .and(user.phoneNumber.notIn(blocks))
                                      .and(user.gender.ne(gender))
                                      .and(user.certification.isTrue())
                                      .and(user.role.eq(Role.USER))
                                      .and(buildRegionalFilter(city, user));
  }

  private BooleanExpression isCreatedToday(QUser user, LocalDate today) {
    return user.createdDate.year()
                           .eq(today.getYear())
                           .and(user.createdDate.month().eq(today.getMonthValue()))
                           .and(user.createdDate.dayOfMonth().eq(today.getDayOfMonth()));
  }

  private BooleanExpression isCreatedCardToday(LocalDate today) {
    return card.createdDate.year()
                           .eq(today.getYear())
                           .and(card.createdDate.month().eq(today.getMonthValue()))
                           .and(card.createdDate.dayOfMonth().eq(today.getDayOfMonth()));
  }

  private BooleanExpression buildSmokingPreferenceFilter(Smoke smoke) {
    if (smoke == Smoke.NONE) {
      return userDetail.smoke.eq(smoke);
    } else {
      return userDetail.smoke.notIn(Smoke.NONE);
    }
  }

  private BooleanExpression buildRegionalFilter(String city, QUser user) {
    if (METROPOLITAN_AREA.contains(city)) {
      return user.address.city.in(METROPOLITAN_AREA);
    } else {
      return user.address.city.eq(city);
    }
  }

}
