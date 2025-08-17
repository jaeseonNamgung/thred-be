package com.thred.datingapp.community.service;

import com.testFixture.CommunityFixture;
import com.testFixture.UserFixture;
import com.thred.datingapp.common.entity.community.Comment;
import com.thred.datingapp.common.entity.community.Community;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.CommunityErrorCode;
import com.thred.datingapp.community.dto.request.CommentRequest;
import com.thred.datingapp.community.repository.CommentRepository;
import com.thred.datingapp.community.repository.CommunityRepository;
import com.thred.datingapp.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository   commentRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private CommentService      sut;


    @Test
    @DisplayName("parentCommentId가 0인 경우 부모 없는 댓글(최상위 댓글)로 저장된다")
    void createComment_whenParentIdIsZero_thenSavesAsTopLevelComment() {
        // given
        Long userId = 1L;
        User user = UserFixture.createTestUser(1);
        Community community = CommunityFixture.createCommunity(1, user);
        CommentRequest commentRequest = CommentRequest.of(0L, "content", true);
        ReflectionTestUtils.setField(user, "id", 1L);
        Comment parentComment = CommunityFixture.createParentComment(1, community, user);
        ReflectionTestUtils.setField(parentComment, "id", 1L);
        given(userService.getUserById(anyLong())).willReturn(user);
        given(commentRepository.save(any())).willReturn(parentComment);
        // when
        sut.createComment(community, userId, commentRequest);
        // then
        then(userService).should().getUserById(anyLong());
        then(commentRepository).should(never()).findById(anyLong());
        then(commentRepository).should().save(any());
    }

    @Test
    @DisplayName("parentCommentId가 0이 아닌 경우 자식 댓글로 저장된다.")
    void createComment_whenParentIdIsNotZero_thenSavesChildComment() {
        // given
        Long userId = 1L;
        CommentRequest commentRequest = CommentRequest.of(5L, "content", true);
        User user = UserFixture.createTestUser(1);
        ReflectionTestUtils.setField(user, "id", 1L);
        Community community = CommunityFixture.createCommunity(1, user);
        Comment parentComment = CommunityFixture.createParentComment(1, community, user);
        ReflectionTestUtils.setField(parentComment, "id", 1L);
        Comment childComment = CommunityFixture.createChildComment(1, community, 1L, user);
        ReflectionTestUtils.setField(childComment, "id", 1L);

        given(userService.getUserById(anyLong())).willReturn(user);
        given(commentRepository.findById(anyLong())).willReturn(Optional.of(parentComment));
        given(commentRepository.save(any())).willReturn(childComment);
        // when
        sut.createComment(community, userId, commentRequest);
        // then
        then(userService).should().getUserById(anyLong());
        then(commentRepository).should().findById(anyLong());
        then(commentRepository).should().save(any());
    }

    /*
    * @Author NamgungJaeseon
    * @Description
    * 1. 회원 아이디와 댓글 아이디로 댓글 조회
    *   - 없으면 에러 호출
    * 2. 댓글 메시지 변경후 DB에 저장
    * @Date 2024.03.08
    * */
    @Test
    @DisplayName("[댓글 삭제] 회원 아이디와 댓글 아이디로 조회한 댓글이 존재하지 않을 경우 에러 호출")
    void deleteCommentTestCase() {
        // given
        given(commentRepository.findByCommentIdAndUserId(any(), any())).willReturn(Optional.empty());
        // when
        CustomException expectedException = assertThrows(CustomException.class,()-> sut.deleteComment(1L, 1L));
        // then
        assertThat(expectedException.getErrorCode().getHttpStatus()).isEqualTo(CommunityErrorCode.NOT_FOUND_COMMENT.getHttpStatus());
        assertThat(expectedException.getErrorCode().getMessage()).isEqualTo(CommunityErrorCode.NOT_FOUND_COMMENT.getMessage());
        then(commentRepository).should().findByCommentIdAndUserId(any(), any());
    }
    @Test
    @DisplayName("댓글 아이디로 댓글을 삭제(댓글은 삭제 되지 않고 삭제 메시지로 대체)")
    void deleteCommentTestCase2() {
      // given
        Comment comment = createComment();
        given(commentRepository.findByCommentIdAndUserId(any(), any())).willReturn(Optional.of(comment));
      // when
        Boolean expectedTrue = sut.deleteComment(1L, 1L);
      // then
        assertThat(expectedTrue).isTrue();
        then(commentRepository).should().findByCommentIdAndUserId(any(), any());
    }

    private Comment createComment() {
        User user = User.builder()
                .username("testUser")
                .email("test@test.com")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Community community = Community.builder().build();
        return Comment.builder()
                .content("create comment content")
                .user(user)
                .community(community)
                .build();
    }

}
