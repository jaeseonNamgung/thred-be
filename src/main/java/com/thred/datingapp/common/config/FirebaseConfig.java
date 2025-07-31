package com.thred.datingapp.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.ChatErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@ToString
@Slf4j
@Configuration
public class FirebaseConfig {

    private final String accountKey;

    public FirebaseConfig(
            @Value("${firebase.service.account-key}")String accountKey
    ) {
        this.accountKey = accountKey;
    }

    @PostConstruct
    public void init() {

        try {
            InputStream refreshToken = new ByteArrayInputStream(accountKey.getBytes());
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(refreshToken))
                    .build();
            if(FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            log.info("[init] FirebaseApp 초기화 성공");
        } catch (IOException e) {
            log.error("[init] firebase 초기화 에러 ===> errorMessage: {}", e.getMessage());
            throw new CustomException(ChatErrorCode.NOTIFICATION_ERROR);
        }
    }
}
