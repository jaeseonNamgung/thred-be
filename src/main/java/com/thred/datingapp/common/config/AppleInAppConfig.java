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
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Slf4j
@EnableConfigurationProperties(AppleInAppProperties.class)
@Configuration
public class AppleInAppConfig {

    @Bean
    public SignedDataVerifier signedDataVerifier(AppleInAppProperties appleInAppProperties) {
        return new SignedDataVerifier(
                loadRootCAs(appleInAppProperties),
                appleInAppProperties.getBundleId(),
                appleInAppProperties.getAppleId(),
                Environment.SANDBOX,
                true
        );
    }

    @Bean
    public AppStoreServerAPIClient appStoreServerAPIClient(AppleInAppProperties appleInAppProperties) {
        if (appleInAppProperties.getPrivateKey() == null) {
            log.warn("[AppStoreServerAPIClient] PrivateKey가 설정되지 않았습니다.");
            return null;  // FIXME: PrivateKey 설정 후 수정 필요
        }
        return new AppStoreServerAPIClient(
                appleInAppProperties.getPrivateKey(),
                appleInAppProperties.getKeyId(),
                appleInAppProperties.getIssuerId(),
                appleInAppProperties.getBundleId(),
                Environment.LOCAL_TESTING
        );
    }

    private Set<InputStream> loadRootCAs(AppleInAppProperties appleInAppProperties) {
        try {
            log.debug("Loading root CAs...");
            return Set.of(
                    new FileInputStream("apple/AppleRootCA-G2.cer"),
                    new FileInputStream("apple/AppleRootCA-G3.cer")
            );
        }catch (IOException e) {
            log.error("[loadRootCAs] rootCA 파일 경로 에러(Failed to load root CA file) ===> errorMessage: {}", e.getMessage());
            throw new CustomException(InAppErrorCode.APPLE_CA_ERROR, e);
        } catch (Exception e) {
            log.error("[loadRootCAs] rootCA 목록을 로드하는 중 알 수 없는 오류 발생 (Unexpected error while loading root CAs) ===> errorMessage: {}", e.getMessage());
            throw new CustomException(InAppErrorCode.APPLE_CA_ERROR, e);
        }
    }
}
