package com.thred.datingapp.inApp.dto.response;

import com.thred.datingapp.common.entity.inApp.type.PurchaseType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThreadUseHistoryResponse{
  private Long threadUserHistoryId;
  private String purchaseTypeName;
  private String purchaseName;
  private int     amount;
  private LocalDateTime createdDate;

  public ThreadUseHistoryResponse(Long threadUserHistoryId, PurchaseType purchaseType, int amount, LocalDateTime createdDate) {
    this.threadUserHistoryId = threadUserHistoryId;
    this.purchaseTypeName = purchaseType.name();
    this.purchaseName = purchaseType.getDescription();
    this.amount = amount;
    this.createdDate = createdDate;
  }
}
