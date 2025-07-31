package com.thred.datingapp.community.service;

import com.thred.datingapp.common.entity.community.Comment;
import com.thred.datingapp.common.entity.community.Community;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.CommunityErrorCode;
import com.thred.datingapp.community.dto.request.CommentRequest;
import com.thred.datingapp.community.dto.response.CommentResponse;
import com.thred.datingapp.community.repository.CommentLikeRepository;
import com.thred.datingapp.community.repository.CommentRepository;
import com.thred.datingapp.community.repository.CommunityRepository;
import com.thred.datingapp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentService {

  private static final String DELETE_MESSAGE       = "삭제된 댓글입니다.";
  private static final String ADMIN_DELETE_MESSAGE = "관리자에 의해 삭제된 댓글입니다.";

  private final CommunityRepository   communityRepository;
  private final CommentRepository     commentRepository;
  private final UserService           userService;
  private final CommentLikeRepository commentLikeRepository;

  @Transactional
  public CommentResponse createComment(final Long communityId, final Long userId, final CommentRequest commentRequest) {
    Community community = getCommunityById(communityId);
    User user = userService.getUserById(userId);

    // parent id == 0: 자식 댓글, parent id == null: 부모 댓글
    Comment savedComment;
    if (commentRequest.parentCommentId() == null || commentRequest.parentCommentId() == 0) {
      savedComment = commentRepository.save(commentRequest.toCommentEntity(community, user));
    } else {
      savedComment = commentRepository.findById(commentRequest.parentCommentId())
                                      .map(parent -> commentRepository.save(commentRequest.toCommentEntity(community, user, parent.getId())))
                                      .orElseGet(() -> commentRepository.save(commentRequest.toCommentEntity(community, user)));

    }
    log.info("[createComment] 댓글 저장 완료(Successfully created comment) ===> comment: {}", savedComment);
    return CommentResponse.from(savedComment, 0, false, userId, List.of());
  }

  @Transactional
  public boolean addCommentLike(final Long communityId, final Long commentId, final Long userId) {
    // 좋아요 업데이트 구현
    if (commentId == null || userId == null || communityId == null) {
      log.warn("[addCommentLike] commentId or userId or communityId is null ===> communityId: {}, commentId: {}, userId: {}", communityId, commentId,
               userId);
      return false;
    }
    // 사용자가 작성한 댓글인지 확인
    boolean isExistCommentLike = commentLikeRepository.existsLikesByCommentIdAndUserId(commentId, userId);
    if (isExistCommentLike) {
      log.warn("[addCommentLike] 사용자가 작성한 댓글입니다. ===> commentId: {},userId: {}", commentId, userId);
      return false;
    }
    commentLikeRepository.insertLikeByCommentIdAndUserIdAndCommunityId(commentId, userId, communityId);
    log.info("[addCommentLike] 댓글 저장 완료(Successfully created comment like) ===> commentId: {}, userId: {}, communityId: {}", commentId, userId,
             communityId);
    return true;
  }

  @Transactional
  public boolean deleteCommentLike(final Long commentId, final Long userId) {
    // 좋아요 삭제 구현
    if (commentId == null || userId == null) {
      log.warn("[deleteCommentLike] commentId or userId is null ===> communityId: {}, userId: {}", commentId, userId);
      return false;
    }
    boolean isExistCommentLike = commentLikeRepository.existsLikesByCommentIdAndUserId(commentId, userId);
    if (!isExistCommentLike) {
      log.warn("[deleteCommentLike] isExistCommentLike is null");
      return false;
    }
    commentLikeRepository.deleteLikeByCommentIdAndUserId(commentId, userId);
    log.info("[deleteCommentLike] 댓글 삭제 완료(Successfully deleted comment like) ===> commentId: {}, userId: {}", commentId, userId);
    return true;
  }

  @Transactional
  public Boolean deleteComment(final Long commentId, final Long userId) {
    return deleteCommentInternal(commentId, userId, DELETE_MESSAGE, false);
  }

  @Transactional
  public void deleteCommentByAdmin(final Long commentId, final Long userId) {
    deleteCommentInternal(commentId, userId, ADMIN_DELETE_MESSAGE, true);
  }

  private Boolean deleteCommentInternal(final Long commentId, final Long userId, final String deleteMessage, boolean isAdmin) {
    Comment comment = commentRepository.findByCommentIdAndUserId(commentId, userId).orElseThrow(() -> {
      log.error("[deleteComment{}] 존재하지 않은 댓글 (Not found comment) ===> commentId: {}, userId: {}", isAdmin ? "ByAdmin" : "", commentId, userId);
      return new CustomException(CommunityErrorCode.NOT_FOUND_COMMENT);
    });

    comment.deleteComment(deleteMessage);

    log.info("[deleteComment{}] 댓글 삭제 완료 (Successfully deleted comment) ===> commentId: {}, isDeleted: {}, content: {}", isAdmin ? "ByAdmin" : "",
             commentId, comment.isDelete(), comment.getContent());

    return true;
  }
  private Community getCommunityById(Long communityId) {
    return communityRepository.findById(communityId).orElseThrow(() -> {
      log.error("[createComment] 존재하지 않은 게시글입니다. ===> communityId: {}", communityId);
      return new CustomException(CommunityErrorCode.NOT_FOUND_BOARD);
    });
  }
}
