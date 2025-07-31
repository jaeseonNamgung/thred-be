package com.thred.datingapp.community.dto.request;

import com.thred.datingapp.common.entity.community.Comment;
import com.thred.datingapp.common.entity.community.Community;
import com.thred.datingapp.common.entity.user.User;

public record CommentRequest(
    Long parentCommentId,
    String content,
    boolean isPublicProfile
) {
  public static CommentRequest of(Long parentCommentId, String content, boolean isPublicProfile) {
    return new CommentRequest(parentCommentId, content, isPublicProfile);
  }

  public Comment toCommentEntity(Community community, User user, Long parentId) {
    return Comment.builder()
        .content(content)
        .isPublicProfile(isPublicProfile)
        .parentId(parentId)
        .user(user)
        .community(community)
        .build();
  }
  public Comment toCommentEntity(Community community, User user) {
    return Comment.builder()
                  .content(content)
                  .isPublicProfile(isPublicProfile)
                  .user(user)
                  .community(community)
                  .build();
  }
}
