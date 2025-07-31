package com.thred.datingapp.community.repository.queryDsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thred.datingapp.common.entity.community.Comment;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.thred.datingapp.common.entity.community.QComment.comment;

@RequiredArgsConstructor
public class CommentQueryDslImpl implements CommentQueryDsl {
  private final JPAQueryFactory queryFactory;
  @Override
  public List<Comment> findByCommunityId(Long communityId) {
    return queryFactory.selectFrom(comment)
        .leftJoin(comment.user).fetchJoin()
        .where(comment.community.id.eq(communityId))
        .orderBy(comment.createdDate.desc())
        .fetch();
  }
}
