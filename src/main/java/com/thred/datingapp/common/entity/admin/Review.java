package com.thred.datingapp.common.entity.admin;

import com.thred.datingapp.common.entity.BaseEntity;
import com.thred.datingapp.common.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
public class Review extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long         id;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_Id")
  private User         user;
  @Enumerated(value = EnumType.STRING)
  private ReviewStatus reviewStatus;
  @Enumerated(value = EnumType.STRING)
  private ReviewType   reviewType;
  public  String       reason;

  public void updateReviewStatus(ReviewStatus reviewStatus, String reason) {
    this.reviewStatus = reviewStatus;
    this.reason = reviewStatus == ReviewStatus.FAIL ? reason : null;
  }

  @Builder
  public Review(User user, ReviewStatus reviewStatus, ReviewType reviewType, String reason) {
    this.user = user;
    this.reviewStatus = reviewStatus;
    this.reviewType = reviewType;
    this.reason = reason;
  }
}
