package com.thred.datingapp.common.entity.admin;

import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.common.error.CustomException;

import java.util.Arrays;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
public enum ReviewStatus {
  PENDING, // 심사 대기
  SUCCESS, // 심사 성공
  FAIL, // 심사 실패
  ;

  public static ReviewStatus findStatus(String value) {
    return Arrays.stream(ReviewStatus.values())
                 .filter(judgmentResult -> judgmentResult.name().equals(value.toUpperCase()))
                 .findAny()
                 .orElseGet(() -> {
                   log.error("[findResult] 존재하지 않은 Result 타입입니다. ===> type: {}", value.toUpperCase());
                   throw new CustomException(UserErrorCode.INVALID_TYPE);
                 });
  }
}
