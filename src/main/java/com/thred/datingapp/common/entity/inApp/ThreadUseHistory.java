package com.thred.datingapp.common.entity.inApp;

import com.thred.datingapp.common.entity.BaseEntity;
import com.thred.datingapp.common.entity.inApp.type.PurchaseType;
import com.thred.datingapp.common.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class ThreadUseHistory extends BaseEntity {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Comment("구매 대상 유저 ID (실타래 충전 시에는 0으로 초기화)")
  @Column(nullable = false)
  private Long purchaseTargetUserId;

  @Comment("구매 타입")
  @Enumerated(EnumType.STRING)
  private PurchaseType purchaseType;

  @Comment("실타래 수량")
  @Column(nullable = false)
  private int amount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Builder
  public ThreadUseHistory(
          Long purchaseTargetUserId,
          PurchaseType purchaseType,
          int amount,
          User user) {
    this.purchaseTargetUserId = purchaseTargetUserId;
    this.purchaseType = purchaseType;
    this.amount = amount;
    this.user = user;
  }

}
