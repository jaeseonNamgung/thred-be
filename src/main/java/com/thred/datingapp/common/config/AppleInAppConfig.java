package com.thred.datingapp.common.config;

import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.model.Environment;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.InAppErrorCode;
import com.thred.datingapp.inApp.properties.AppleInAppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@Slf4j
@EnableConfigurationProperties(AppleInAppProperties.class)
@Configuration
public class AppleInAppConfig {

  /**
   * SignedDataVerifier Bean 생성
   * - App Store Server에서 전달되는 서명된 데이터(JWS)를 검증하기 위한 객체
   * - Apple Root CA 인증서를 로드하여 서명 체인을 검증
   *
   * @param appleInAppProperties
   *     앱스토어 연동 관련 설정 값 (bundleId, appleId 등)
   *
   * @return SignedDataVerifier 인스턴스
   */
  @Bean
  public SignedDataVerifier signedDataVerifier(AppleInAppProperties appleInAppProperties) {
    log.info("[signedDataVerifier] SignedDataVerifier 초기화 시작");
    SignedDataVerifier signedDataVerifier = new SignedDataVerifier(loadRootCAs(),                          // Root CA 세트 (Apple 인증서 체인)
                                                                   appleInAppProperties.getBundleId(),     // 앱 번들 ID
                                                                   appleInAppProperties.getAppleId(),      // Apple 계정 ID (in-app 결제 확인용)
                                                                   Environment.SANDBOX,                    // 환경 설정 (테스트 환경: SANDBOX, 운영: PRODUCTION)
                                                                   true);                                  // CRL(인증서 폐지 목록) 확인 여부
    log.info("[signedDataVerifier] SignedDataVerifier 초기화 성공");
    return signedDataVerifier;

  }

  /**
   * Apple Root CA 인증서 파일을 로드하는 메서드
   * - Apple 서버에서 오는 서명 데이터를 검증할 때 필요한 신뢰 anchor
   * - .cer 파일을 InputStream으로 읽어 SignedDataVerifier에 전달
   *
   * @return Apple Root CAs(InputStream Set)
   * @throws CustomException
   *     APPLE_CA_ERROR: 파일을 못 찾거나 읽을 수 없는 경우
   */
  private Set<InputStream> loadRootCAs() {
    try{
      log.debug("Loading root CAs...");
      // Apple Root CA (G2, G3) 인증서 로드
      InputStream g2 = getClass().getClassLoader().getResourceAsStream("apple/AppleRootCA-G2.cer");
      InputStream g3 = getClass().getClassLoader().getResourceAsStream("apple/AppleRootCA-G3.cer");
      if (g2 == null || g3 == null) {
        log.error("[loadRootCAs] rootCA 파일 경로 에러 (Failed to load root CA file)");
        throw new CustomException(InAppErrorCode.APPLE_CA_ERROR);
      }
      return Set.of(g2, g3);
    } catch (Exception e) {
      // 알 수 없는 기타 오류
      log.error("[loadRootCAs] rootCA 목록을 로드하는 중 알 수 없는 오류 발생 (Unexpected error while loading root CAs) ===> errorMessage: {}", e.getMessage());
      throw new CustomException(InAppErrorCode.APPLE_CA_ERROR, e);
    }
  }

  /**
   * App Store Server API 클라이언트를 생성하는 Bean 정의 메서드
   * - Apple In-App Purchase 서버와 통신하기 위해 필요한 인증 정보 및 키를 기반으로 초기화
   * - .p8 인증 키 파일을 읽어와 AppStoreServerAPIClient 인스턴스를 반환
   *
   * @param appleInAppProperties
   *     AppleInAppProperties 설정 객체 (keyId, issuerId, bundleId 포함)
   *
   * @return AppStoreServerAPIClient 인스턴스
   */
  @Bean
  public AppStoreServerAPIClient appStoreServerAPIClient(AppleInAppProperties appleInAppProperties) {
    log.info("[appStoreServerAPIClient] AppStoreServerAPIClient 초기화 시작");
    // AppStoreServerAPIClient 객체 생성
    // - apiKey : Apple이 발급한 private key (JWT 서명에 필요)
    // - keyId : App Store Connect에서 발급한 Key ID
    // - issuerId : App Store Connect API Key의 Issuer ID
    // - bundleId : 앱 번들 식별자
    // - Environment.SANDBOX : 테스트 환경 (운영 시 PRODUCTION으로 변경 가능)
    AppStoreServerAPIClient apiClient =
        new AppStoreServerAPIClient(appleInAppProperties.getApiKey(),
                                    appleInAppProperties.getKeyId(),
                                    appleInAppProperties.getIssuerId(),
                                    appleInAppProperties.getBundleId(),
                                    Environment.SANDBOX);
    log.info("[appStoreServerAPIClient] AppStoreServerAPIClient 초기화 성공");
    return apiClient;
  }

}
