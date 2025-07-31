package com.thred.datingapp.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.user.dto.KakaoDto;
import com.thred.datingapp.user.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Slf4j
@RequiredArgsConstructor
@Service
public class OAuthService {

  private final ObjectMapper objectMapper;
  @Value("${oauth.kakao.user-info-uri}")
  private String kakaoUserInfoUri;

  public KakaoDto getKakaoUserInfo(String accessToken) {
    CloseableHttpClient client = HttpClientBuilder.create().build();
    HttpGet get = new HttpGet(kakaoUserInfoUri);
    get.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + accessToken);

    try (CloseableHttpResponse response = client.execute(get)){
      String responseBody = EntityUtils.toString(response.getEntity());
      KakaoDto kakaoDto = objectMapper.readValue(responseBody, KakaoDto.class);
      log.debug("[getKakaoUserInfo] 카카오 사용자 정보 조회 성공 - id: {}", kakaoDto.id());
      return objectMapper.readValue(responseBody, KakaoDto.class);
    } catch (IOException e) {
      log.error("[getKakaoUserInfo] 카카오 회원 정보 조회 오류");
      throw new CustomException(UserErrorCode.INVALID_SOCIAL_RESPONSE);
    }
  }
}
