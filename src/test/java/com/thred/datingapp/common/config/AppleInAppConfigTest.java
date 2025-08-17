package com.thred.datingapp.common.config;

import com.apple.itunes.storekit.client.APIException;
import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.model.SendTestNotificationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

@ActiveProfiles("local")
@SpringBootTest
class AppleInAppConfigTest {

  @Autowired
  private AppStoreServerAPIClient client;


  @Test
  void Apple_JWT_API_요청_테스트() {
    try {
      SendTestNotificationResponse response = client.requestTestNotification();
      System.out.println(response);
    } catch (APIException | IOException e) {
      e.printStackTrace();
    }
  }



}
