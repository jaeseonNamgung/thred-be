package com.thred.datingapp.common.api.response;

public record ApiStatusResponse(boolean status) {
  public static ApiStatusResponse of(boolean status) {
    return new ApiStatusResponse(status);
  }
}
