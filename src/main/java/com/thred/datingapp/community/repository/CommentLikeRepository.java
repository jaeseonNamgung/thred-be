package com.thred.datingapp.community.repository;

import com.thred.datingapp.common.entity.community.CommentLike;
import com.thred.datingapp.community.repository.queryDsl.CommentLikeQueryDsl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long>, CommentLikeQueryDsl {

  @Modifying(clearAutomatically = true)
  @Query(nativeQuery = true, value = "INSERT INTO comment_like (comment_id, user_id,community_id, created_date) VALUES(:commentId, :userId, :communityId, now())")
  void insertLikeByCommentIdAndUserIdAndCommunityId(@Param("commentId")Long commentId, @Param("userId")Long userId, @Param("communityId")Long communityId);

  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM CommentLike WHERE commentLikePk.commentId = :commentId AND commentLikePk.userId = :userId")
  void deleteLikeByCommentIdAndUserId(@Param("commentId")Long commentId, @Param("userId")Long userId);

  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM CommentLike WHERE commentLikePk.commentId = :communityId")
  void deleteLikeByCommunityId(@Param("communityId")Long communityId);

  @Query("SELECT COUNT(c) FROM CommentLike c WHERE c.commentLikePk.commentId = :commentId")
  int countByCommentLikePkCommentId(@Param("commentId") Long commentId);
}
