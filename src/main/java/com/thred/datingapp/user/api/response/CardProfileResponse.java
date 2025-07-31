package com.thred.datingapp.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CardProfileResponse {

  private Long   cardId;
  private Long   userId;
  private String username;
  private String mainProfile;
  private int    age;
  private int    height;
  private String province;
  private int    temperature;
}
