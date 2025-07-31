package com.thred.datingapp.community.repository.queryDsl;

public interface CommentLikeQueryDsl {
  boolean existsLikesByCommentIdAndUserId(Long comment, Long userId);
}
