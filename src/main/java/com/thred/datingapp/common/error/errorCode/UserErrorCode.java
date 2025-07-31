package com.thred.datingapp.common.error.errorCode;

import com.thred.datingapp.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

  // 공통 & 로그인
  AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
  USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않은 사용자입니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
  UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰 형식입니다."),
  NOT_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "RefreshToken 토큰이 아닙니다."),
  MISSING_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "Refresh 토큰이 없습니다."),
  SUSPENDED_USER(HttpStatus.UNAUTHORIZED, "정지된 사용자입니다."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 서비스에 대한 권한이 없습니다."),

  // 회원 가입
  ALREADY_REGISTERED_EMAIL(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),
  REJECT_HISTORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "심사 거부 이력이 없어 재가입을 진행할 수 없습니다."),
  JOIN_HISTORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "이전 가입 이력이 존재하지 않습니다."),
  INVALID_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 코드입니다."),
  ALREADY_REGISTERED_USER(HttpStatus.BAD_REQUEST, "이미 가입된 사용자입니다."),
  ROLE_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 역할은 존재하지 않습니다"),
  MAIN_PROFILE_REQUIRED(HttpStatus.BAD_REQUEST, "메인 프로필은 반드시 1장 있어야 합니다."),
  INSUFFICIENT_PROFILE_PHOTOS(HttpStatus.BAD_REQUEST, "사진은 총 4장 이상이어야 합니다."),
  INVALID_SMOKING_STATUS(HttpStatus.BAD_REQUEST, "흡연 정보가 정확하지 않습니다."),
  INVALID_DRINKING_STATUS(HttpStatus.BAD_REQUEST, "음주 정보가 정확하지 않습니다."),
  INVALID_RELIGION_STATUS(HttpStatus.BAD_REQUEST, "종교 정보가 정확하지 않습니다."),
  INVALID_GENDER_STATUS(HttpStatus.BAD_REQUEST, "성별 정보가 정확하지 않습니다."),
  INVALID_JOB_STATUS(HttpStatus.BAD_REQUEST, "직업 정보가 정확하지 않습니다."),
  INVALID_MBTI_STATUS(HttpStatus.BAD_REQUEST, "MBTI 정보가 정확하지 않습니다."),
  INVALID_OPPOSITE_FRIENDS_STATUS(HttpStatus.BAD_REQUEST, "이성 친구 정보가 정확하지 않습니다."),
  INVALID_PARTNER_GENDER_STATUS(HttpStatus.BAD_REQUEST, "상대 성별 정보가 정확하지 않습니다."),

  // 관리자
  INVALID_TYPE(HttpStatus.BAD_REQUEST, "요청 파라미터의 타입이 올바르지 않습니다."),
  REVIEW_NOT_FOUND(HttpStatus.BAD_REQUEST, "Review 내역이 존재하지 않습니다."),
  ADMIN_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 관리자입니다."),
  DECLARATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 Declaration ID가 유효하지 않습니다."),
  NO_COMPLETED_JUDGMENTS(HttpStatus.BAD_REQUEST, "완료된 인증이 없습니다."),
  INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "파라미터 값을 올바르게 입력해주세요."),

  // 소셜 로그인
  INVALID_SOCIAL_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "소셜 로그인 응답 중 오류입니다.")
  ;
  private final HttpStatus httpStatus;
  private final String     message;
}
