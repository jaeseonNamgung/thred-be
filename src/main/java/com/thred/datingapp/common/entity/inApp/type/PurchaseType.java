package com.thred.datingapp.common.entity.inApp.type;

import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

@Slf4j
@ToString
@Getter
@RequiredArgsConstructor
public enum PurchaseType {

  REFERRAL_CODE("referralCode", "추천인 코드", 10),
  ANSWER_QUESTION("answerQuestion", "상대 질문 답변", 7),
  VIEW_PROFILE("viewProfile", "프로필 오픈", 2),
  THREAD_TOP_UP("threadTopUp","실타래 충전", 0),
  ;

  private final String typeName;
  private final String description;
  private final int    amount;

  public static PurchaseType findType(String type) {
    return Stream.of(values()).filter(purchaseType -> purchaseType.name().equalsIgnoreCase(type)).findFirst().orElseThrow(() -> {
      log.error("[findType] 존재하지 않은 타입입니다. ===> purchaseType: {}", type);
      return new CustomException(BaseErrorCode.TYPE_MISMATCH);
    });
  }
}
