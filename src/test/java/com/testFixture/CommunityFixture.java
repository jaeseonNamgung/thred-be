package com.testFixture;

import com.thred.datingapp.common.entity.community.Comment;
import com.thred.datingapp.common.entity.community.CommentLike;
import com.thred.datingapp.common.entity.community.Community;
import com.thred.datingapp.common.entity.community.CommunityImage;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.field.Gender;
import com.thred.datingapp.community.dto.request.CommunityRequest;
import com.thred.datingapp.community.dto.response.CommunityAllResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

public class CommunityFixture {

  public static Community createCommunity(int i, User user) {
    Community community =
        Community.builder().title("community title" + i).content("community content" + i).isPublicProfile(i % 2 != 0) // 홀수: true, 짝수: false
                 .user(user).build();
    ReflectionTestUtils.setField(community, "id", (long) i);
    return community;
  }

  public static Comment createParentComment(int i, Community community, User user) {
    Comment comment = Comment.builder().content("parent comment content" + i).isPublicProfile(i % 2 != 0) // 홀수: true, 짝수: false
                             .community(community).user(user).build();
    ReflectionTestUtils.setField(comment, "id", (long) i);
    return comment;
  }


  public static Comment createChildComment(int i, Community community, Long parentId, User user) {
    Comment childComment = Comment.builder().content("child comment content" + i).isPublicProfile(i % 2 != 0) // 홀수: true, 짝수: false
                                  .community(community).user(user).parentId(parentId).build();
    ReflectionTestUtils.setField(childComment, "id", (long) i);
    return childComment;
  }

  public static CommunityAllResponse createCommunityAllResponse() {
    CommunityAllResponse communityAllResponse = new CommunityAllResponse();
    ReflectionTestUtils.setField(communityAllResponse, "communityId", 1L);
    ReflectionTestUtils.setField(communityAllResponse, "title", "Test Title");
    ReflectionTestUtils.setField(communityAllResponse, "image", "test.jpg");
    ReflectionTestUtils.setField(communityAllResponse, "likeCount", 10L);
    ReflectionTestUtils.setField(communityAllResponse, "commentCount", 5L);
    ReflectionTestUtils.setField(communityAllResponse, "userId", 100L);
    ReflectionTestUtils.setField(communityAllResponse, "nickName", "TestUser");
    ReflectionTestUtils.setField(communityAllResponse, "profile", "profile.jpg");
    ReflectionTestUtils.setField(communityAllResponse, "gender", Gender.MALE.getGender());
    ReflectionTestUtils.setField(communityAllResponse, "statusLike", true);
    ReflectionTestUtils.setField(communityAllResponse, "communityType", "");
    ReflectionTestUtils.setField(communityAllResponse, "createdDate", LocalDateTime.now());
    return communityAllResponse;
  }

  public static CommunityRequest createCommunityUpdateRequest() {
    return CommunityRequest.of("updateTitle", "updateContent", true);
  }

  public static CommunityImage createCommunityImage(Community community) {
    CommunityImage communityImage =
        CommunityImage.builder().s3Path("https://example.com/image.jpg").originalFileName("image.jpg").community(community).build();
    ReflectionTestUtils.setField(communityImage, "id", 1L);
    return communityImage;
  }

  public static CommunityRequest createCommunityRequest() {
    return CommunityRequest.of("title", "content", true);
  }

}
