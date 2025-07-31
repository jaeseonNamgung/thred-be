package com.thred.datingapp.community.repository.queryDsl;

import com.thred.datingapp.common.entity.community.Comment;

import java.util.List;

public interface CommentQueryDsl {
  List<Comment> findByCommunityId(Long communityId);
}
