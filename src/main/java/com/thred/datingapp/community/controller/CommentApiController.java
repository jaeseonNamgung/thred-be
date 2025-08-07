package com.thred.datingapp.community.controller;

import com.thred.datingapp.common.api.response.ApiDataResponse;
import com.thred.datingapp.common.error.errorCode.CommunityErrorCode;
import com.thred.datingapp.community.dto.request.CommentRequest;
import com.thred.datingapp.community.dto.response.CommentResponse;
import com.thred.datingapp.community.service.CommentService;
import com.thred.datingapp.user.argumentResolver.Login;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api/comment")
@RequiredArgsConstructor
@RestController
public class CommentApiController {
  private final CommentService commentService;

  @PostMapping("/create/{communityId}")
  public ApiDataResponse<CommentResponse> createComment(
          @PathVariable("communityId") Long communityId,
          @RequestBody CommentRequest commentRequest,
          @Login Long userId
  ) {
    log.info("[API CALL] /api/comment/create/{communityId} - 댓글 저장 요청");
    log.debug("[createComment] communityId: {}", communityId);
    log.debug("[createComment] userId: {}", userId);
    log.debug("[createComment] commentRequest: {}", commentRequest);
    return ApiDataResponse.ok(commentService.createComment(communityId, userId, commentRequest));
  }

  @PostMapping("/add/like/{communityId}/{commentId}")
  public ApiDataResponse<Boolean> addLike(
          @PathVariable("communityId") Long communityId,
          @PathVariable("commentId") Long commentId,
          @Login Long userId
  ) {
    log.info("[API CALL] /api/comment/add/like/{communityId}/{commentId} - 댓글 좋아요 추가 요청");
    log.debug("[addLike] communityId: {}", communityId);
    log.debug("[addLike] commentId: {}", commentId);
    log.debug("[addLike] userId: {}", userId);
    return commentService.addCommentLike(communityId, commentId, userId) ?
            ApiDataResponse.ok(true):
            ApiDataResponse.ok(false, CommunityErrorCode.ALREADY_ADDED_LIKED.getMessage());
  }
  @DeleteMapping("/delete/like/{commentId}")
  public ApiDataResponse<Boolean> deleteLike(
          @PathVariable("commentId") Long commentId,
          @Login Long userId
  ) {
    log.info("[API CALL] /api/comment/delete/like/{commentId} - 댓글 좋아요 삭제 요청");
    log.debug("[deleteLike] commentId: {}", commentId);
    log.debug("[deleteLike] userId: {}", userId);
    return commentService.deleteCommentLike(commentId, userId) ?
            ApiDataResponse.ok(true):
            ApiDataResponse.ok(false, CommunityErrorCode.ALREADY_REMOVED_LIKE.getMessage());
  }
  @DeleteMapping("/delete/{commentId}")
  public ApiDataResponse<Boolean> deleteComment(
          @PathVariable("commentId") Long commentId,
          @Login Long userId
  ) {
    log.info("[API CALL] /api/comment/delete/{commentId} - 댓글 삭제 요청");
    log.debug("[deleteComment] commentId: {}", commentId);
    log.debug("[deleteComment] userId: {}", userId);
      return ApiDataResponse.ok(commentService.deleteComment(commentId, userId));
  }
}
