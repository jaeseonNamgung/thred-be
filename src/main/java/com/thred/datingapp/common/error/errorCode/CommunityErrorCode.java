package com.thred.datingapp.common.error.errorCode;

import com.thred.datingapp.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum CommunityErrorCode implements ErrorCode {

  NOT_FOUND_BOARD(HttpStatus.BAD_REQUEST, "이미 삭제된 게시글입니다."),
  NOT_FOUND_COMMENT(HttpStatus.BAD_REQUEST, "존재하지 않은 댓글입니다."),
  ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 게시글입니다."),
  UPDATE_FAILED(HttpStatus.BAD_REQUEST, "업데이트할 수 없는 게시글입니다."),
  ALREADY_ADDED_LIKED(HttpStatus.NOT_FOUND, "이미 좋아요를 추가했습니다."),
  ALREADY_REMOVED_LIKE(HttpStatus.NOT_FOUND, "이미 제거된 좋아요입니다.");

  private final HttpStatus httpStatus;
  private final String     message;

}
