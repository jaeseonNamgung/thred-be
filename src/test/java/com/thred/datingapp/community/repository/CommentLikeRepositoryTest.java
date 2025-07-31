package com.thred.datingapp.community.repository;

import com.testFixture.CommunityFixture;
import com.testFixture.UserFixture;
import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.community.Comment;
import com.thred.datingapp.common.entity.community.Community;
import com.thred.datingapp.common.entity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Import({JpaConfig.class, P6SpyConfig.class})
@DataJpaTest
class CommentLikeRepositoryTest {

  @Autowired
  private CommentLikeRepository commentLikeRepository;
  @Autowired
  private TestEntityManager     entityManager;


  @Test
  @DisplayName("댓글 유무 테스트")
  void existsLikesByCommentTest() {

    // 존재하지 않을 때
    setUp(100);
    boolean isFalse = commentLikeRepository.existsLikesByCommentIdAndUserId(2L, 1L);
    assertThat(isFalse).isFalse();

    // 존재할 때

    Comment comment = entityManager.find(Comment.class, 1L);
    commentLikeRepository.insertLikeByCommentIdAndUserIdAndCommunityId(comment.getId(), comment.getUser().getId(), comment.getCommunity().getId());
    boolean isTrue = commentLikeRepository.existsLikesByCommentIdAndUserId(comment.getId(), comment.getUser().getId());
    assertThat(isTrue).isTrue();
  }
  @Test
  @DisplayName("댓글 좋아요 삭제 테스트")
  void deleteLikeByCommentIdAndUserId() {

    // 삭제 전
    setUp(100);
    Comment comment = entityManager.find(Comment.class, 1L);
    commentLikeRepository.insertLikeByCommentIdAndUserIdAndCommunityId(comment.getId(), comment.getUser().getId(), comment.getCommunity().getId());
    boolean isTrue = commentLikeRepository.existsLikesByCommentIdAndUserId(comment.getId(), comment.getUser().getId());
    assertThat(isTrue).isTrue();

    // 삭제 후
    commentLikeRepository.deleteLikeByCommentIdAndUserId(comment.getId(), comment.getUser().getId());
    boolean isFalse = commentLikeRepository.existsLikesByCommentIdAndUserId(comment.getId(), comment.getUser().getId());
    assertThat(isFalse).isFalse();
  }
  @Test
  @DisplayName("좋아요 수 조회 테스트")
  void countByCommentLikePkCommentId() {

    setUp(100);
    Comment comment = entityManager.find(Comment.class, 1L);
    commentLikeRepository.insertLikeByCommentIdAndUserIdAndCommunityId(1L, 1L, comment.getCommunity().getId());
    commentLikeRepository.insertLikeByCommentIdAndUserIdAndCommunityId(1L, 2L, comment.getCommunity().getId());
    commentLikeRepository.insertLikeByCommentIdAndUserIdAndCommunityId(1L, 3L, comment.getCommunity().getId());
    commentLikeRepository.insertLikeByCommentIdAndUserIdAndCommunityId(1L, 4L, comment.getCommunity().getId());
    commentLikeRepository.insertLikeByCommentIdAndUserIdAndCommunityId(1L, 5L, comment.getCommunity().getId());

    entityManager.flush();
    entityManager.clear();

    int count = commentLikeRepository.countByCommentLikePkCommentId(comment.getId());
    assertThat(count).isEqualTo(5);
  }
  @Test
  @DisplayName("특정 커뮤니티 댓글 좋아요 전체 삭제")
  void deleteLikeByCommunityId() {

    setUp(100);
    Comment comment = entityManager.find(Comment.class, 1L);
    commentLikeRepository.insertLikeByCommentIdAndUserIdAndCommunityId(1L, 1L, comment.getCommunity().getId());
    commentLikeRepository.insertLikeByCommentIdAndUserIdAndCommunityId(1L, 2L, comment.getCommunity().getId());
    commentLikeRepository.insertLikeByCommentIdAndUserIdAndCommunityId(1L, 3L, comment.getCommunity().getId());
    commentLikeRepository.insertLikeByCommentIdAndUserIdAndCommunityId(1L, 4L, comment.getCommunity().getId());
    commentLikeRepository.insertLikeByCommentIdAndUserIdAndCommunityId(1L, 5L, comment.getCommunity().getId());

    entityManager.flush();
    entityManager.clear();

    commentLikeRepository.deleteLikeByCommunityId(comment.getCommunity().getId());
    boolean isFalse = commentLikeRepository.existsLikesByCommentIdAndUserId(comment.getId(), comment.getUser().getId());
    assertThat(isFalse).isFalse();
  }



  private void setUp(int count) {
    User user = UserFixture.createTestUser(1);
    entityManager.persist(user);
    Community community = CommunityFixture.createCommunity(1, user);
    entityManager.persist(community);
    for (int i = 2; i <= count; i++) {
      User testUser = UserFixture.createTestUser(i);
      entityManager.persist(testUser);
      Comment parentComment = CommunityFixture.createParentComment(1, community, testUser);
      entityManager.persist(parentComment);
    }
    entityManager.flush();
    entityManager.clear();

  }
}
