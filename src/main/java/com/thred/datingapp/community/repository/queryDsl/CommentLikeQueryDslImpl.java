package com.thred.datingapp.community.repository.queryDsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.thred.datingapp.common.entity.community.QCommentLike.commentLike;

@RequiredArgsConstructor
public class CommentLikeQueryDslImpl implements CommentLikeQueryDsl {
  private final JPAQueryFactory queryFactory;

  @Override
  public boolean existsLikesByCommentIdAndUserId(Long commentId, Long userId) {
    return queryFactory.selectOne()
                       .from(commentLike)
                       .where(commentLike.commentLikePk.commentId.eq(commentId), commentLike.commentLikePk.userId.eq(userId))
                       .fetchFirst() != null;
  }
}
