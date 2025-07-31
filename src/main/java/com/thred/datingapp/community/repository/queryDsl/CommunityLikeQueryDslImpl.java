package com.thred.datingapp.community.repository.queryDsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.thred.datingapp.common.entity.community.QCommunityLike.communityLike;

@RequiredArgsConstructor
public class CommunityLikeQueryDslImpl implements CommunityLikeQueryDsl {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsLikesByCommunityIdAndUserId(Long communityId, Long userId) {
        return queryFactory.selectOne()
                .from(communityLike)
                .where(communityLike.communityLikePk.communityId.eq(communityId), communityLike.communityLikePk.userId.eq(userId))
                .fetchFirst() != null;
    }
}
