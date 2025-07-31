package com.thred.datingapp.user.api.response;

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
