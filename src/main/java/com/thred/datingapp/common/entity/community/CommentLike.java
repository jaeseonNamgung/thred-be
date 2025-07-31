package com.thred.datingapp.common.entity.community;

import com.thred.datingapp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class CommentLike extends BaseEntity {

  @EmbeddedId
  private CommentLikePk commentLikePk;
  @Column(nullable = false)
  private Long communityId;
}
