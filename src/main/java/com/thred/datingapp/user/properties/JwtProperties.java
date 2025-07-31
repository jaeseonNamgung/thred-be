package com.thred.datingapp.user.properties;

public interface JwtProperties {
  Long   ACCESS_EXPIRATION_TIME  = 14 * 24 * 60 * 60 * 1000L;
  Long   REFRESH_EXPIRATION_TIME = 14 * 24 * 60 * 60 * 1000L;
  String TOKEN_PREFIX            = "Bearer ";
  String HEADER_STRING           = "Authorization";
  String ACCESS_TOKEN            = "access";
  String REFRESH_TOKEN           = "refresh";
  String SOCIAL_ID               = "socialId";
  String CATEGORY                = "category";
  String USER_ID                 = "userId";
  String ROLE                    = "role";
}
