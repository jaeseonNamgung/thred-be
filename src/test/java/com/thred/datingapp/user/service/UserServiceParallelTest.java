package com.thred.datingapp.user.service;

import com.testFixture.UserFixture;
import com.thred.datingapp.user.api.request.JoinDetailsRequest;
import com.thred.datingapp.user.api.request.JoinUserRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@ActiveProfiles("local")
@SpringBootTest
class UserServiceParallelTest {
  private static final int TOTAL_COUNT = 3;
  private ExecutorService executorService;
  private CountDownLatch latch;
  @Autowired
  private UserService userService;

  @BeforeEach
  void init() {
    executorService = Executors.newFixedThreadPool(TOTAL_COUNT);
    latch = new CountDownLatch(TOTAL_COUNT);
  }

  @Test
  void 회원_가입_동시성_테스트() throws InterruptedException {
    JoinUserRequest joinUserRequest = UserFixture.createJoinUserRequest(1);
    JoinDetailsRequest joinDetailsRequest = UserFixture.createJoinDetailsRequest(1);
    MultipartFile multipartFile = UserFixture.createMultipartFile(1);
    List<MultipartFile> multipartFiles = UserFixture.createMultipartFiles(1);
    for (int i = 0; i < TOTAL_COUNT; i++) {

      executorService.submit(()->{
        try {
          userService.join(joinUserRequest, joinDetailsRequest, multipartFile, multipartFiles);
        } catch (Exception e) {
          log.error("Exception", e);
        } finally {
          latch.countDown();
        }
      });

    }
    latch.await();
  }
}
