package com.thred.datingapp.chat.repository.queryDsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thred.datingapp.common.entity.chat.QFcmToken;
import lombok.RequiredArgsConstructor;

import java.util.Optional;


@RequiredArgsConstructor
public class FcmTokenQueryDslImpl implements FcmTokenQueryDsl{

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<String> findByMemberUserId(Long userId) {
        String fcmToken = queryFactory.select(QFcmToken.fcmToken.token).from(QFcmToken.fcmToken)
                .where(QFcmToken.fcmToken.user.id.eq(userId))
                .fetchOne();
        return Optional.ofNullable(fcmToken);
    }
}
