package com.thred.datingapp.community.repository;

import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.community.Comment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, P6SpyConfig.class})
@DataJpaTest
class CommentRepositoryTest {

  @Autowired
  private CommentRepository commentRepository;

  @Test
  @DisplayName("[QueryDsl] 댓글 전체 조회 테스트 ")
  void findAllComment() {
      // given

      // when
    List<Comment> comments = commentRepository.findByCommunityId(1L);
    // then
    List<Comment> parentComments = comments.stream()
            .filter(comment -> comment.getParentId() == null)
            .toList();

    assertThat(parentComments)
            .extracting("content")
            .containsAnyOf("삭제된 댓글입니다.", "Parent comment 2 on Post 1.");

    List<Comment> children = comments.stream()
            .filter(comment -> comment.getParentId() != null)
            .toList();

    assertThat(children)
            .extracting("content")
            .containsAnyOf("삭제된 댓글입니다.", "Child comment 1 under Parent comment 2.");

  }

  @Test
  @DisplayName("[QueryDsl] 댓글 수 조회 테스트")
  void commentCountTest() {
      // given

      // when
    int expectedCount = commentRepository.commentCount(1L);
    // then
    assertThat(expectedCount).isEqualTo(11);
  }

}
