package com.thred.datingapp.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CommunityAllResponse {
  private Long          communityId;
  private String        title;
  private String        image;
  private Long          likeCount;
  private Long          commentCount;
  private Long          userId;
  private String        nickName;
  @Setter
  private String        profile;
  private String        gender;
  private Boolean       statusLike;
  private String        communityType;
  private LocalDateTime createdDate;
}
