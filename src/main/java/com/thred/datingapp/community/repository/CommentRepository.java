package com.thred.datingapp.community.repository;

import com.thred.datingapp.common.entity.community.Comment;
import com.thred.datingapp.community.repository.queryDsl.CommentQueryDsl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentQueryDsl {
    @Query("select count(*) from Comment cm where cm.community.id = :communityId")
    int commentCount(@Param("communityId") Long communityId);

    @Modifying(clearAutomatically = true)
    @Query("update Comment cm set cm.user = null where cm.user.id = :userId")
    void detachUserFromComments(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Comment cm where cm.community.id = :communityId")
    void deleteAllByCommunityId(@Param("communityId") Long communityId);

    @Query("select c from Comment c where c.id = :commentId and c.user.id = :userId")
    Optional<Comment> findByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId")Long userId);

//    @Modifying(clearAutomatically = true)
//    @Query("delete from Comment cm where cm.community.id = :communityId and cm.parent is null")
//    void deleteParentByCommentId(@Param("communityId") Long communityId);
//
//    @Modifying(clearAutomatically = true)
//    @Query("delete from Comment cm where cm.community.id = :communityId and cm.parent is not null")
//    void deleteChildByCommunityId(@Param("communityId") Long communityId);

    @Query("select cm from Comment cm where cm.parentId = :parentId")
    List<Comment> findByParentId(@Param("parentId") Long parentId);

    @Query("select cm.id from Comment cm where cm.id = :parentId")
    Long findParentIdByParentId(@Param("parentId") Long parentId);

}
