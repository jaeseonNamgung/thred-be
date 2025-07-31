package com.thred.datingapp.community.dto.response;

import com.thred.datingapp.common.entity.community.Comment;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        long commentId,
        long userId,
        long parentCommentId,
        String username,
        String profile,
        String content,
        boolean isAuthor,
        boolean isPublicProfile,
        boolean isDelete,
        int likeCount,
        boolean statusLike,
        List<CommentResponse> childrenComment,
        LocalDateTime createdDate
) {
    public static CommentResponse from(Comment comment, int likeCount, boolean statusLike, long userId, List<CommentResponse> childrenComment) {
      long parentCommentId = comment.getParentId() == null || comment.getParentId() == 0L ? 0L : comment.getParentId();
      return new CommentResponse(
                comment.getId(),
                comment.getUser().getId(),
                parentCommentId ,
                comment.getUser().getUsername(),
                comment.getUser().getMainProfile(),
                comment.getContent(),
                comment.getUser().getId().equals(userId),
                comment.isPublicProfile(),
                comment.isDelete(),
                likeCount,
                statusLike,
                childrenComment,
                comment.getCreatedDate()
        );
    }
}
