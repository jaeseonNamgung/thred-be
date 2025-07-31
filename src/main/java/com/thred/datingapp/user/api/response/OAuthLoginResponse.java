package com.thred.datingapp.user.api.response;

public record OAuthLoginResponse(
    Long userId,
    boolean status,
    boolean certification,
    String role,
    String accessToken,
    String refreshToken
) {
  public static OAuthLoginResponse ofSuccess(
      Long userId,
      String role,
      String accessToken,
      String refreshToken
  ){
    return new OAuthLoginResponse(userId, true, true, role, accessToken, refreshToken);
  }
  public static OAuthLoginResponse ofFailure(){
    return new OAuthLoginResponse(null, false, false, null, null, null);
  }
  public static OAuthLoginResponse ofAuthorizationFailure(Long userId, String role){
    return new OAuthLoginResponse(userId, true, false, role, null, null);
  }
}
