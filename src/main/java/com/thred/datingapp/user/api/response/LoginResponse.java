package com.thred.datingapp.user.api.response;

import com.thred.datingapp.common.entity.admin.ReviewStatus;

public record LoginResponse(
        boolean status,
        boolean certification,
        String role
) {
  public static LoginResponse of(
      boolean status,
      boolean certification,
      String role
  ) {
    return new LoginResponse(status, certification, role);
  }
}
