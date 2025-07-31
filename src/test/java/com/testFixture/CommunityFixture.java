package com.testFixture;

import com.thred.datingapp.common.entity.community.Comment;
import com.thred.datingapp.common.entity.community.CommentLike;
import com.thred.datingapp.common.entity.community.Community;
import com.thred.datingapp.common.entity.user.User;

public class CommunityFixture {

  public static Community createCommunity(int i, User user) {
    return Community.builder()
        .title("community title"+i)
        .content("community content"+i)
        .isPublicProfile(i%2!=0) // 홀수: true, 짝수: false
        .user(user)
        .build();
  }
  public static Comment createParentComment(int i, Community community, User user) {
    return Comment.builder()
        .content("parent comment content"+i)
        .isPublicProfile(i%2!=0) // 홀수: true, 짝수: false
        .community(community)
        .user(user)
        .build();
  }
  public static Comment createChildComment(int i, Community community,Long parentId, User user) {
    return Comment.builder()
                  .content("parent comment content"+i)
                  .isPublicProfile(i%2!=0) // 홀수: true, 짝수: false
                  .community(community)
                  .user(user)
                  .parentId(parentId)
                  .build();
  }
}
