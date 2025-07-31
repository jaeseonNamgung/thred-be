package com.thred.datingapp.main.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
public class CardOpenResponse {

  private Long   cardId;
  private Long   profileUserId;
  private String mainProfile;
}
