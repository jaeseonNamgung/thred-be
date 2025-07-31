package com.thred.datingapp.community.repository.queryDsl;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thred.datingapp.common.entity.community.Community;
import com.thred.datingapp.common.entity.community.QCommunity;
import com.thred.datingapp.common.entity.community.type.CommunityType;
import com.thred.datingapp.community.dto.response.CommunityAllResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Optional;

import static com.thred.datingapp.common.entity.community.QComment.comment;
import static com.thred.datingapp.common.entity.community.QCommunity.community;
import static com.thred.datingapp.common.entity.community.QCommunityImage.communityImage;
import static com.thred.datingapp.common.entity.community.QCommunityLike.communityLike;
import static com.thred.datingapp.common.entity.user.QUser.user;

@RequiredArgsConstructor
public class CommunityQueryDslImpl implements CommunityQueryDsl {
    private final JPAQueryFactory queryFactory;

    @Value("${community.hot.like.threshold}")
    private long hotLikeThreshold;

    @Override
    public Optional<Community> findByCommunityId(Long communityId) {

        Community content = queryFactory.selectFrom(community)
                .distinct()
                .leftJoin(community.user).fetchJoin()
                .leftJoin(community.communityImages).fetchJoin()
                .where(community.id.eq(communityId))
                .fetchOne();
        return Optional.ofNullable(content);
    }

    @Override
    public Page<CommunityAllResponse> findCommunitiesByCommunityTypeAndPageLastIdWithPaging
            (CommunityType communityType, Long pageLastId, int pageSize) {
        NumberPath<Long> likeCountPath = Expressions.numberPath(Long.class, "likeCount");
        NumberPath<Long> commentCountPath = Expressions.numberPath(Long.class, "commentCount");

        JPQLQuery<Long> subLikeCount = JPAExpressions.select(communityLike.count())
                .from(communityLike)
                .where(communityLike.communityLikePk.communityId.eq(community.id));

        JPQLQuery<Long> subCommentCount = JPAExpressions.select(comment.count())
                .from(comment)
                .where(comment.community.id.eq(community.id));

        JPQLQuery<String> subImage = JPAExpressions.select(communityImage.s3Path)
                .from(communityImage)
                .where(
                        communityImage.id.eq(
                                JPAExpressions.select(communityImage.id.min())
                                        .from(communityImage)
                                        .where(communityImage.community.id.eq(community.id))
                        )
                );
        BooleanExpression existsLike = JPAExpressions.select(communityLike)
                .from(communityLike)
                .where(communityLike.communityLikePk.communityId.eq(community.id), communityLike.communityLikePk.userId.eq(user.id))
                .exists();

        List<CommunityAllResponse> contents = queryFactory.select(
                        Projections.fields(
                                CommunityAllResponse.class,
                                community.id.as("communityId"),
                                community.title.as("title"),
                                Expressions.asString(subImage).as("image"),
                                ExpressionUtils.as(subLikeCount, likeCountPath),
                                ExpressionUtils.as(subCommentCount, commentCountPath),
                                community.user.id.as("userId"),
                                community.user.username.as("nickName"),
                                Expressions.stringTemplate("CASE WHEN {0} = 'MALE' THEN '남성' WHEN {0} = 'FEMALE' THEN '여성' END", community.user.gender).as("gender"),
                                existsLike.as("statusLike"),
                                Expressions.asString(communityType.getDescription()).as("communityType"),
                                community.createdDate.as("createdDate")))
                .from(community)
                .leftJoin(community.user, user)
                .where(communityTypeCondition(communityType), pageLastIdCondition(pageLastId))
                .limit(pageSize)
                .orderBy(community.createdDate.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(community.count()).from(community);

        return PageableExecutionUtils.getPage(contents, Pageable.ofSize(pageSize), countQuery::fetchOne);
    }

    // FIXME findCommunitiesByUseIdAndPageLastIdWithPaging, findCommunitiesByCommunityTypeAndPageLastIdWithPaging
    //       리팩토링 필요 (중복 기능이 너무 많음)
    @Override
    public Page<CommunityAllResponse> findCommunitiesByUseIdAndPageLastIdWithPaging(Long userId, Long pageLastId, int pageSize) {
        NumberPath<Long> likeCountPath = Expressions.numberPath(Long.class, "likeCount");
        NumberPath<Long> commentCountPath = Expressions.numberPath(Long.class, "commentCount");

        JPQLQuery<Long> subLikeCount = JPAExpressions.select(communityLike.count())
                .from(communityLike)
                .where(communityLike.communityLikePk.communityId.eq(community.id));

        JPQLQuery<Long> subCommentCount = JPAExpressions.select(comment.count())
                .from(comment)
                .where(comment.community.id.eq(community.id));

        JPQLQuery<String> subImage = JPAExpressions.select(communityImage.s3Path)
                .from(communityImage)
                .where(
                        communityImage.id.eq(
                                JPAExpressions.select(communityImage.id.min())
                                        .from(communityImage)
                                        .where(communityImage.community.id.eq(community.id))
                        )
                );
        BooleanExpression existsLike = JPAExpressions.select(communityLike)
                .from(communityLike)
                .where(communityLike.communityLikePk.communityId.eq(community.id), communityLike.communityLikePk.userId.eq(user.id))
                .exists();

        List<CommunityAllResponse> contents = queryFactory.select(
                        Projections.constructor(
                                CommunityAllResponse.class,
                                community.id.as("communityId"),
                                community.title.as("title"),
                                Expressions.asString(subImage).as("image"),
                                ExpressionUtils.as(subLikeCount, likeCountPath),
                                ExpressionUtils.as(subCommentCount, commentCountPath),
                                community.user.id.as("userId"),
                                community.user.username.as("nickName"),
                                community.user.mainProfile.as("mainProfile"),
                                Expressions.stringTemplate("CASE WHEN {0} = 'MALE' THEN '남성' WHEN {0} = 'FEMALE' THEN '여성' END", community.user.gender).as("gender"),
                                existsLike.as("statusLike"),
                                Expressions.nullExpression(String.class),
                                community.createdDate.as("createdDate")))
                .from(community)
                .leftJoin(community.user, user)
                .where(community.user.id.eq(userId), pageLastIdCondition(pageLastId))
                .limit(pageSize)
                .orderBy(community.createdDate.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(community.count()).from(community).where(community.user.id.eq(userId));

        return PageableExecutionUtils.getPage(contents, Pageable.ofSize(pageSize), countQuery::fetchOne);

    }

    @Override
    public Long updateByCommunityId(Community updateCommunity) {
        return queryFactory.update(community)
                .set(community.title, updateCommunity.getTitle())
                .set(community.content, updateCommunity.getContent())
                .set(community.isPublicProfile, updateCommunity.getIsPublicProfile())
                .where(community.id.eq(updateCommunity.getId()))
                .execute();
    }

    @Override
    public boolean existsByCommunityIdAndUserId(Long communityId, Long userId) {
        return queryFactory.selectOne()
                .from(QCommunity.community)
                .where(QCommunity.community.id.eq(communityId), QCommunity.community.user.id.eq(userId))
                .fetchFirst() != null;
    }

    private BooleanExpression pageLastIdCondition(Long pageLastId) {
        QCommunity community = QCommunity.community;
        return pageLastId != null && pageLastId > 0 ? community.id.lt(pageLastId) : null;
    }

    private BooleanExpression communityTypeCondition(CommunityType communityType) {
        if (communityType.equals(CommunityType.HOT)) {
            return JPAExpressions
                    .select(communityLike.count())
                    .from(communityLike)
                    .where(communityLike.communityLikePk.communityId.eq(community.id))
                    .goe(hotLikeThreshold);
        }
        return null;
    }
}
