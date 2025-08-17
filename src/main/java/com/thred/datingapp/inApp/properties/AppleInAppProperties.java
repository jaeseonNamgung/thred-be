package com.thred.datingapp.inApp.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Getter
@ConfigurationPropertiesScan
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "apple.in-app")
public class AppleInAppProperties {

    private final String bundleId;
    private final String issuerId;
    private final String keyId;
    private final Long appleId;
    private final String apiKey;
    private final String rootCA2; // 인증서 만료 기간: Apr 30, 2039
    private final String rootCA3; // 인증서 만료 기간: Apr 30, 2039

}
