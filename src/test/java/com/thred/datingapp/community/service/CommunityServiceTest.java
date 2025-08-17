package com.thred.datingapp.community.service;

import com.testFixture.CommunityFixture;
import com.testFixture.UserFixture;
import com.thred.datingapp.common.api.response.PageResponse;
import com.thred.datingapp.common.entity.community.Comment;
import com.thred.datingapp.common.entity.community.Community;
import com.thred.datingapp.common.entity.community.CommunityImage;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.field.Gender;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.CommunityErrorCode;
import com.thred.datingapp.community.dto.request.CommunityRequest;
import com.thred.datingapp.community.dto.response.CommunityAllResponse;
import com.thred.datingapp.community.dto.response.CommunityResponse;
import com.thred.datingapp.community.repository.*;
import com.thred.datingapp.common.utils.S3Utils;
import com.thred.datingapp.user.repository.PictureRepository;
import com.thred.datingapp.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

  @Mock
  private CommunityRepository      communityRepository;
  @Mock
  private CommunityImageRepository communityImageRepository;
  @Mock
  private CommentService        commentService;
  @Mock
  private CommunityLikeRepository  communityLikeRepository;

  @Mock
  private S3Utils           s3Utils;
  @Mock
  private UserService       userService;
  @Mock
  private PictureRepository pictureRepository;

  @InjectMocks
  private CommunityService sut;

  @Test
  void testGetUserProfileRandomIndex() {
    // given
    Long communityId = 123L;
    int imageLength = 4;

    // 동일한 key 로직을 테스트 코드에서 복제
    LocalDate today = LocalDate.of(2025, 6, 19); // 테스트용 고정 날짜
    String key = today + "-" + communityId;
    int expectedIndex = Math.abs(key.hashCode()) % imageLength;
    System.out.println(key.hashCode());
    System.out.println(expectedIndex);

    // when
    int actualIndex = getUserProfileRandomIndexTest(communityId, imageLength, today);

    // then
    assertThat(expectedIndex).isEqualTo(actualIndex);
  }

  private int getUserProfileRandomIndexTest(Long communityId, int imageLength, LocalDate today) {
    String key = today + "-" + communityId;
    return Math.abs(key.hashCode()) % imageLength;
  }

  @Test
  @DisplayName("커뮤니티 게시글 생성 테스트 - 이미지 존재")
  void createCommunityTestCase() throws IOException {
    // given
    final String fileName = "testImage1"; //파일명
    final String contentType = "png"; //파일타입

    User user = UserFixture.createTestUser(1);
    Community community = CommunityFixture.createCommunity(1, user);
    Community savedCommunity = CommunityFixture.createCommunity(1, user);
    CommunityFixture.createCommunityImage(savedCommunity);
    String s3Url = "s3Url";
    CommunityRequest communityRequest = CommunityFixture.createCommunityRequest();
    MockMultipartFile multipartFile = new MockMultipartFile("images", //name
                                                            fileName + "." + contentType, //originalFilename
                                                            contentType, "test data".getBytes());

    given(userService.getUserById(any())).willReturn(user);
    given(communityRepository.save(any())).willReturn(community);
    given(s3Utils.saveImage(any())).willReturn(s3Url);
    given(communityImageRepository.saveAll(any())).willReturn(community.getCommunityImages());
    given(communityRepository.findByCommunityId(any())).willReturn(Optional.of(savedCommunity));
    given(pictureRepository.findS3PathAllByUserId(anyLong())).willReturn(List.of("test1.jpg", "test2.jpg", "test3.jpg", "test4.jpg"));
    // when
    CommunityResponse expectedResponse = sut.createCommunity(1L, communityRequest, List.of(multipartFile));
    // then
    assertThat(expectedResponse).hasFieldOrPropertyWithValue("title", community.getTitle())
                                .hasFieldOrPropertyWithValue("content", community.getContent())
                                .hasFieldOrPropertyWithValue("isPublicProfile", community.getIsPublicProfile())
                                .hasFieldOrProperty("images")
                                .isNotNull();

    then(userService).should().getUserById(any());
    then(communityRepository).should().save(any());
    then(s3Utils).should().saveImage(any());
    then(communityImageRepository).should().saveAll(any());
    then(communityRepository).should().findByCommunityId(any());
    then(pictureRepository).should().findS3PathAllByUserId(anyLong());
  }

  @Test
  @DisplayName("커뮤니티 게시글 생성 테스트 - 이미지 존재 X")
  void createCommunityTestCase2() throws IOException {
    // given
    // User 객체 생성
    User user = UserFixture.createTestUser(1);
    // Community 객체 생성
    Community community = Community.builder().title("title").content("content").isPublicProfile(true).user(user)  // User 객체 연결
                                   .build();
    ReflectionTestUtils.setField(community, "id", 1L);
    CommunityRequest communityRequest = CommunityFixture.createCommunityRequest();

    given(userService.getUserById(any())).willReturn(user);
    given(communityRepository.save(any())).willReturn(community);
    given(communityRepository.findByCommunityId(any())).willReturn(Optional.of(community));
    given(pictureRepository.findS3PathAllByUserId(anyLong())).willReturn(List.of("test1.jpg", "test2.jpg", "test3.jpg", "test4.jpg"));

    // when
    CommunityResponse expectedResponse = sut.createCommunity(1L, communityRequest, null);
    // then
    assertThat(expectedResponse).hasFieldOrPropertyWithValue("title", community.getTitle())
                                .hasFieldOrPropertyWithValue("content", community.getContent())
                                .hasFieldOrPropertyWithValue("isPublicProfile", community.getIsPublicProfile());
    assertThat(expectedResponse.images()).isEmpty();

    then(userService).should().getUserById(any());
    then(communityRepository).should().save(any());
    then(s3Utils).should(times(0)).saveImage(any());
    then(communityImageRepository).should(times(0)).saveAll(any());
    then(communityRepository).should().findByCommunityId(any());
    then(pictureRepository).should().findS3PathAllByUserId(anyLong());
  }

  @Test
  @DisplayName("[Update Community Likes] 좋아요 하나 증가")
  void addCommunityLikeTestCase() throws Exception {
    //give
    Long communityId = 1L;
    Long userId = 1L;
    given(communityLikeRepository.existsLikesByCommunityIdAndUserId(any(), any())).willReturn(false);
    //when
    boolean expectedLikes = sut.addCommunityLike(communityId, userId);
    //then
    assertThat(expectedLikes).isTrue();
    then(communityLikeRepository).should().existsLikesByCommunityIdAndUserId(any(), any());
    then(communityLikeRepository).should().insertLikeByCommunityIdAndUserId(any(), any());

  }

  @Test
  @DisplayName("[Update Community Likes] communityId가 존재하지 않으면 false를 리턴")
  void addCommunityLikeTestCase3() throws Exception {
    //give
    Long userId = 1L;
    //when
    boolean expectedLikes = sut.addCommunityLike(null, userId);
    //then
    assertThat(expectedLikes).isFalse();
  }

  @Test
  @DisplayName("[Update Community Likes] 좋아요가 존재하면 false를 리턴")
  void addCommunityLikeTestCase2() throws Exception {
    //give
    Long communityId = 1L;
    Long userId = 1L;
    given(communityLikeRepository.existsLikesByCommunityIdAndUserId(any(), any())).willReturn(true);
    //when
    boolean expectedLikes = sut.addCommunityLike(communityId, userId);
    //then
    assertThat(expectedLikes).isFalse();
    then(communityLikeRepository).should().existsLikesByCommunityIdAndUserId(any(), any());
  }

  @Test
  @DisplayName("[Update Community Likes] 좋아요 하나 증감")
  void deleteCommunityLikeTestCase() throws Exception {
    //give
    Long communityId = 1L;
    Long userId = 1L;
    given(communityLikeRepository.existsLikesByCommunityIdAndUserId(any(), any())).willReturn(true);
    //when
    boolean expectedLikes = sut.deleteCommunityLike(communityId, userId);
    //then
    assertThat(expectedLikes).isTrue();
    then(communityLikeRepository).should().existsLikesByCommunityIdAndUserId(any(), any());
    then(communityLikeRepository).should().deleteLikeByCommunityIdAndUserId(any(), any());
  }

  @Test
  @DisplayName("[Update Community Likes] 특정 회원에 좋아요가 존재하지 않으면 false 리턴")
  void deleteCommunityLikeTestCase2() throws Exception {
    //give
    Long communityId = 1L;
    Long userId = 1L;
    given(communityLikeRepository.existsLikesByCommunityIdAndUserId(any(), any())).willReturn(false);
    //when
    boolean expectedLikes = sut.deleteCommunityLike(communityId, userId);
    //then
    assertThat(expectedLikes).isFalse();
    then(communityLikeRepository).should().existsLikesByCommunityIdAndUserId(any(), any());
  }

  @Test
  @DisplayName("커뮤니티 단건 조회 테스트")
  void getCommunity() {
    // given
    User user = UserFixture.createTestUser(1);
    Community community = CommunityFixture.createCommunity(1, user);
    CommunityFixture.createCommunityImage(community);
    Comment parentComment = CommunityFixture.createParentComment(1, community, user);
    Comment childComment1 = CommunityFixture.createChildComment(1, community, parentComment.getId(), user);
    Comment childComment2 = CommunityFixture.createChildComment(2, community, parentComment.getId(), user);

    given(communityRepository.findByCommunityId(any())).willReturn(Optional.of(community));
    given(commentService.getAllByCommunityId(any())).willReturn(List.of(parentComment));
    given(commentService.getAllByParentId(any())).willReturn(List.of(childComment1, childComment2));
    given(commentService.existsLikesByCommentIdAndUserId(any(), any())).willReturn(true);
    given(commentService.countByCommentLikePkCommentId(any())).willReturn(2);
    given(pictureRepository.findS3PathAllByUserId(anyLong())).willReturn(List.of("test1.jpg", "test2.jpg", "test3.jpg", "test4.jpg"));

    // when
    CommunityResponse expectedResponse = sut.getCommunity(1L, 1L);
    // then
    assertThat(expectedResponse).isNotNull();
    // 커뮤니티 테스트
    assertThat(expectedResponse.title()).isEqualTo("community title1");
    assertThat(expectedResponse.content()).isEqualTo("community content1");
    // 회원 테스트
    assertThat(expectedResponse.nickName()).isEqualTo("testuser1");

    // 게시글 이미지 테스트
    assertThat(expectedResponse.images().size()).isEqualTo(1);
    assertThat(expectedResponse.images().get(0).s3Path()).isEqualTo("https://example.com/image.jpg");

    // 댓글 테스트 - 부모 댓글
    assertThat(expectedResponse.parentComments().size()).isEqualTo(1);
    assertThat(expectedResponse.parentComments().get(0).content()).isEqualTo("parent comment content1");
    assertThat(expectedResponse.parentComments().get(0).statusLike()).isEqualTo(true);

    // 댓글 테스트 - 자식 댓글
    assertThat(expectedResponse.parentComments().get(0).childrenComment().size()).isEqualTo(2);
    assertThat(expectedResponse.parentComments().get(0).childrenComment().get(0).content()).isEqualTo("child comment content1");
    assertThat(expectedResponse.parentComments().get(0).childrenComment().get(0).statusLike()).isEqualTo(true);
    assertThat(expectedResponse.parentComments().get(0).childrenComment().get(1).content()).isEqualTo("child comment content2");
    assertThat(expectedResponse.parentComments().get(0).childrenComment().get(1).statusLike()).isEqualTo(true);

    then(communityRepository).should().findByCommunityId(any());
    then(commentService).should().getAllByCommunityId(any());
    then(commentService).should().getAllByParentId(any());
    then(commentService).should(times(3)).countByCommentLikePkCommentId(any());
    then(commentService).should(times(3)).existsLikesByCommentIdAndUserId(any(), any());
    then(communityLikeRepository).should().existsLikesByCommunityIdAndUserId(any(), any());
    then(pictureRepository).should().findS3PathAllByUserId(anyLong());

  }

  @Test
  @DisplayName("커뮤니티 단건 조회 시 탈퇴한 회원이 있을 경우 user 관련 반환 값은 null로 처리한다.")
  void getCommunity2() {
    // given
    User user = UserFixture.createTestUser(1);
    Community community = CommunityFixture.createCommunity(1, user);
    Comment parentComment = CommunityFixture.createParentComment(1, community, user);

    given(communityRepository.findByCommunityId(any())).willReturn(Optional.of(community));
    given(commentService.getAllByCommunityId(any())).willReturn(List.of(parentComment));
    given(commentService.countByCommentLikePkCommentId(any())).willReturn(2);
    given(commentService.existsLikesByCommentIdAndUserId(any(), any())).willReturn(true);
    given(communityLikeRepository.existsLikesByCommunityIdAndUserId(any(), any())).willReturn(true);
    given(pictureRepository.findS3PathAllByUserId(anyLong())).willReturn(List.of("test1.jpg", "test2.jpg", "test3.jpg", "test4.jpg"));

    // when
    CommunityResponse expectedResponse = sut.getCommunity(1L, 1L);
    // then
    assertThat(expectedResponse).isNotNull();
    // 커뮤니티 테스트
    assertThat(expectedResponse.title()).isEqualTo("community title1");
    assertThat(expectedResponse.content()).isEqualTo("community content1");
    assertThat(expectedResponse.statusLike()).isEqualTo(true);
    assertThat(expectedResponse.userId()).isEqualTo(1L);
    assertThat(expectedResponse.nickName()).isEqualTo("testuser1");

    // 댓글 테스트 - 부모 댓글
    assertThat(expectedResponse.parentComments().size()).isEqualTo(1);
    assertThat(expectedResponse.parentComments().get(0).content()).isEqualTo("parent comment content1");
    assertThat(expectedResponse.parentComments().get(0).likeCount()).isEqualTo(2);
    assertThat(expectedResponse.parentComments().get(0).statusLike()).isEqualTo(true);
    assertThat(expectedResponse.parentComments().get(0).userId()).isEqualTo(1L);
    assertThat(expectedResponse.parentComments().get(0).username()).isEqualTo("testuser1");

    then(communityRepository).should().findByCommunityId(any());
    then(commentService).should().getAllByCommunityId(any());
    then(commentService).should().countByCommentLikePkCommentId(any());
    then(commentService).should().existsLikesByCommentIdAndUserId(any(), any());
    then(communityLikeRepository).should().existsLikesByCommunityIdAndUserId(any(), any());
    then(pictureRepository).should().findS3PathAllByUserId(anyLong());

  }

  @Test
  @DisplayName("커뮤니티 삭제 테스트 - 이미지가 존재할 경우 s3 좋아요(게시글/댓글) -> image -> 댓글 -> 게세글 순으로 삭제")
  void deleteCommunityTestCase1() {
    // given
    User user = UserFixture.createTestUser(1);
    Community community = CommunityFixture.createCommunity(1, user);
    CommunityImage communityImage = CommunityFixture.createCommunityImage(community);
    given(communityRepository.existsByCommunityIdAndUserId(any(), any())).willReturn(true);
    given(communityImageRepository.findByCommunityId(any())).willReturn(List.of(communityImage));

    // when
    boolean expectedTrue = sut.deleteCommunity(1L, 1L);
    // then
    assertThat(expectedTrue).isTrue();

    then(communityRepository).should().existsByCommunityIdAndUserId(any(), any());
    then(communityImageRepository).should().findByCommunityId(any());
    then(s3Utils).should().deleteS3Image(any());
    then(communityImageRepository).should().deleteByCommunityId(any());
    then(commentService).should().deleteAllByCommunityId(any());
    then(communityRepository).should().deleteByCommunityId(any());
  }

  @Test
  @DisplayName("커뮤니티 삭제 테스트 - 이미지가 존재하지 않을 경우 좋아요(게시글/댓글) -> 댓글 -> 게시글 순으로 삭제")
  void deleteCommunityTestCase2() {
    // given
    given(communityRepository.existsByCommunityIdAndUserId(any(), any())).willReturn(true);
    given(communityImageRepository.findByCommunityId(any())).willReturn(List.of());

    // when
    boolean expectedTrue = sut.deleteCommunity(1L, 1L);
    // then
    assertThat(expectedTrue).isTrue();

    then(communityRepository).should().existsByCommunityIdAndUserId(any(), any());
    then(communityImageRepository).should().findByCommunityId(any());
    then(commentService).should().deleteAllByCommunityId(any());
    then(communityRepository).should().deleteByCommunityId(any());
  }

  @Test
  @DisplayName("커뮤니티 삭제 테스트 - 회원이 작성한 게시글 존재하지 않을 경우 에러 발생")
  void deleteCommunityTestCase3() {
    // given
    given(communityRepository.existsByCommunityIdAndUserId(any(), any())).willReturn(false);

    // when
    CustomException expectedException = (CustomException) catchException(() -> sut.deleteCommunity(1L, 1L));
    // then
    assertThat(expectedException.getErrorCode()).hasFieldOrPropertyWithValue("httpStatus", CommunityErrorCode.NOT_FOUND_BOARD.getHttpStatus())
                                                .hasFieldOrPropertyWithValue("message", CommunityErrorCode.NOT_FOUND_BOARD.getMessage());

    then(communityRepository).should().existsByCommunityIdAndUserId(any(), any());

  }

  @Test
  @DisplayName("회원 탈퇴 시 회원이 작성한 게시글, 댓글과 연관된 회원을 null 처리한 후 true를 리턴")
  void deleteAllCommunitiesForWithdrawnUserTestCase() {
    // given

    // when
    Boolean expectedTrue = sut.deleteAllCommunitiesForWithdrawnUser(1L);
    // then
    assertThat(expectedTrue).isTrue();
    then(userService).should().checkExistsUser(any());
    then(communityRepository).should().detachUserFromCommunities(any());
    then(commentService).should().detachUserFromComments(any());
  }

  @Test
  @DisplayName("게시글 수정 테스트 - 이미지가 존재할 때 s3 -> 기존 이미지 삭제 후 새 이미지를 저장한다.")
  void updateCommunityTestCase1() {
    // given
    final String fileName = "testImage1"; //파일명
    final String contentType = "png"; //파일타입

    User user = UserFixture.createTestUser(1);
    Community community = CommunityFixture.createCommunity(1, user);
    CommunityImage communityImage = CommunityFixture.createCommunityImage(community);
    CommunityRequest communityRequest = CommunityFixture.createCommunityUpdateRequest();
    MockMultipartFile multipartFile = new MockMultipartFile("images", //name
                                                            fileName + "." + contentType, //originalFilename
                                                            contentType, "test data".getBytes());
    given(communityRepository.findByCommunityId(any())).willReturn(Optional.of(community));
    given(communityImageRepository.findByCommunityId(any())).willReturn(List.of(communityImage));
    given(s3Utils.saveImage(any())).willReturn("testImage1.png");
    given(communityRepository.updateByCommunityId(any())).willReturn(1L);
    // when
    Boolean expectedTrue = sut.updateCommunity(1L, communityRequest, List.of(multipartFile));
    // then
    assertThat(expectedTrue).isTrue();

    then(communityImageRepository).should().findByCommunityId(any());
    then(communityImageRepository).should().deleteByCommunityId(any());
    then(s3Utils).should().deleteS3Image(any());
    then(communityImageRepository).should().deleteByCommunityId(any());
    then(s3Utils).should().saveImage(any());
    then(communityImageRepository).should().saveAll(any());
    then(communityRepository).should().updateByCommunityId(any());
  }

  @Test
  @DisplayName("게시글 수정 테스트 - 이미지가 존재하지 않을 경우 새 이미지만 저장한다.")
  void updateCommunityTestCase2() {
    // given
    final String fileName = "testImage1"; //파일명
    final String contentType = "png"; //파일타입

    User user = UserFixture.createTestUser(1);
    Community community = CommunityFixture.createCommunity(1, user);
    CommunityImage communityImage = CommunityFixture.createCommunityImage(community);
    CommunityRequest communityUpdateRequest = CommunityFixture.createCommunityUpdateRequest();
    MockMultipartFile multipartFile = new MockMultipartFile("images", //name
                                                            fileName + "." + contentType, //originalFilename
                                                            contentType, "test data".getBytes());
    given(communityRepository.findByCommunityId(any())).willReturn(Optional.of(community));
    given(communityImageRepository.findByCommunityId(any())).willReturn(List.of(communityImage));
    given(s3Utils.saveImage(any())).willReturn("testImage1.png");
    given(communityRepository.updateByCommunityId(any())).willReturn(1L);
    // when
    Boolean expectedTrue = sut.updateCommunity(1L, communityUpdateRequest, List.of(multipartFile));
    // then
    assertThat(expectedTrue).isTrue();

    then(communityImageRepository).should().findByCommunityId(any());
    then(communityImageRepository).should().deleteByCommunityId(any());
    then(s3Utils).should().saveImage(any());
    then(communityImageRepository).should().saveAll(any());
    then(communityRepository).should().updateByCommunityId(any());
  }

  @Test
  @DisplayName("게시글 수정 테스트 - 새 이미지가 존재하지 않을 경우 게시글만 저장한다.")
  void updateCommunityTestCase4() {
    // given
    User user = UserFixture.createTestUser(1);
    Community community = CommunityFixture.createCommunity(1, user);
    CommunityRequest communityUpdateRequest = CommunityFixture.createCommunityUpdateRequest();
    given(communityRepository.findByCommunityId(any())).willReturn(Optional.of(community));
    given(communityRepository.updateByCommunityId(any())).willReturn(1L);
    // when
    Boolean expectedTrue = sut.updateCommunity(1L, communityUpdateRequest, List.of());
    // then
    assertThat(expectedTrue).isTrue();

    then(communityRepository).should().updateByCommunityId(any());

  }

  @Test
  @DisplayName("특정 회원이 작성한 게시글만 조회")
  void getAllUserCommunityTestCase() {
    // given
    CommunityAllResponse communityAllResponse = CommunityFixture.createCommunityAllResponse();
    given(communityRepository.findCommunitiesByUseIdAndPageLastIdWithPaging(anyLong(), anyLong(), anyInt())).willReturn(
        new PageImpl<>(List.of(communityAllResponse), PageRequest.ofSize(5), 5));
    // when
    PageResponse<CommunityAllResponse> expectedResponse = sut.getAllUserCommunities(1L, 0L, 5);
    // then
    assertThat(expectedResponse).hasFieldOrPropertyWithValue("pageSize", 5).hasFieldOrPropertyWithValue("isLastPage", true);
    assertThat(expectedResponse.contents()).extracting("title", "image", "profile", "userId", "communityId")
                                           .containsExactlyInAnyOrder(tuple("Test Title", "test.jpg", "profile.jpg", 100L, 1L));

    then(communityRepository).should().findCommunitiesByUseIdAndPageLastIdWithPaging(anyLong(), anyLong(), anyInt());
  }


}


