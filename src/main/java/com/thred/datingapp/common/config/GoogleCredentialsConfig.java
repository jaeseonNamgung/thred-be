package com.thred.datingapp.common.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.InAppErrorCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

@Slf4j
@Configuration
public class GoogleCredentialsConfig {

    @Value("${google.in-app.account-key}")
    private String googleApiFile;
    @Getter
    @Value("${google.in-app.package-name}")
    private String packageName;

    @Bean
    public AndroidPublisher androidPublisher() {
        try {
            log.info("[getAccessToken] =========== Google Play API 인증을 위한 액세스 토큰 요청 시작 ===========");
            InputStream inputStream = new ByteArrayInputStream(googleApiFile.getBytes());
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(inputStream)
                    .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));
            googleCredentials.refreshIfExpired();
            log.info("[getAccessToken] =========== Google Play API 액세스 토큰 발급 성공 ===========");
            return new AndroidPublisher.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(googleCredentials)
            ).setApplicationName(packageName).build();

        } catch (IOException e) {
            log.error("[androidPublisher] Google 인증 정보를 불러오는 중 오류가 발생했습니다. 서버 설정을 확인하세요. ===> errorMessage: {}", e.getMessage());
            throw new CustomException(InAppErrorCode.GOOGLE_CREDENTIALS_ERROR, e);
        } catch (Exception e) {
            log.error("[androidPublisher] Google 인증 정보를 처리하는 중 예기치 않은 오류가 발생했습니다. 서버 설정 및 네트워크 상태를 확인하세요. ===> errorMessage: {}", e.getMessage());
            throw new CustomException(InAppErrorCode.GOOGLE_CREDENTIALS_PROCESSING_ERROR, e);
        }
    }
}
