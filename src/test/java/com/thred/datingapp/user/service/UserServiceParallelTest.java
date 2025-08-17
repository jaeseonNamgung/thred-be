package com.thred.datingapp.user.service;

import com.testFixture.UserFixture;
import com.thred.datingapp.common.utils.RedisUtils;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles("local")
@SpringBootTest
class UserServiceParallelTest {
  private static final int TOTAL_COUNT = 3;
  private ExecutorService executorService;
  private CountDownLatch latch;
  @Autowired
  private UserService userService;
  @Autowired
  private RedisUtils  redisUtils;

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

  @Test
  void Address_제거후_캐시_삭제_테스트() {
    String key = "card:daily:viewer:" + 1 + ":" + LocalDate.now();
    redisUtils.saveWithTTL(key, "Test", getSecondsUntilMidnight() , TimeUnit.SECONDS);
    Object o = redisUtils.get(key);
    assertThat(o).isNotNull();
    userService.changeAddress(1L, "28", "28140");
    o = redisUtils.get(key);
    assertThat(o).isNull();
  }

  private long getSecondsUntilMidnight(){
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
    return Duration.between(now, midnight).getSeconds();
  }
}
