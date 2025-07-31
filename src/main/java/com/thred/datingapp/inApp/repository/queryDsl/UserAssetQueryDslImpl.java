package com.thred.datingapp.inApp.repository.queryDsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserAssetQueryDslImpl implements UserAssetQueryDsl {

    private final JPAQueryFactory queryFactory;

}
