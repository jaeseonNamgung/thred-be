package com.thred.datingapp.common.error.errorCode;

import com.thred.datingapp.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MainErrorCode implements ErrorCode {

  ANSWER_NOT_FOUND(HttpStatus.BAD_REQUEST, "답변이 존재하지 않습니다."),
  QUESTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "질문지가 존재하지 않습니다."),
  CARD_ALREADY_CREATED(HttpStatus.BAD_REQUEST, "이미 생성된 카드입니다."),
  CARD_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 카드 ID입니다."),
  ALREADY_OPENED_CARD(HttpStatus.BAD_REQUEST, "이미 오픈된 카드입니다."),
  NO_MORE_TODAY_CARDS(HttpStatus.BAD_REQUEST, "오늘의 카드 다음 페이지가 없습니다."),
  CARD_OPEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않은 오픈된 카드입니다."),
  ;

  private final HttpStatus httpStatus;
  private final String     message;
}
