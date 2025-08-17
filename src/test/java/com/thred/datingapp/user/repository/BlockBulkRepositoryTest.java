package com.thred.datingapp.user.repository;

import com.testFixture.UserFixture;
import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.user.Block;
import com.thred.datingapp.common.entity.user.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Testcontainers
@Import({JpaConfig.class, P6SpyConfig.class, BlockBulkRepository.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class BlockBulkRepositoryTest {

  @Container
  static MySQLContainer<?> mysql  = new MySQLContainer<>("mysql:8.0.35").withReuse(true);;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", () -> mysql.getJdbcUrl()
        + "?rewriteBatchedStatements=true"
        + "&cachePrepStmts=true"
        + "&useServerPrepStmts=true");
    registry.add("spring.datasource.username", mysql::getUsername);
    registry.add("spring.datasource.password", mysql::getPassword);

    // 스키마 자동 생성 (엔티티 기준) - 프로젝트에 맞춰 조정
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    registry.add("spring.jpa.show-sql", () -> "true");

    // JPA 배치 옵션(IDENTITY라 효과 제한적이지만 공정비교 차원에서 설정)
    registry.add("spring.jpa.properties.hibernate.jdbc.batch_size", () -> "100");
    registry.add("spring.jpa.properties.hibernate.order_inserts", () -> "true");
    registry.add("spring.jpa.properties.hibernate.order_updates", () -> "true");
  }

  @Autowired
  private BlockRepository blockRepository;
  @Autowired
  private BlockBulkRepository blockBulkRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private JdbcTemplate   jdbcTemplate;               // 카운트/정리용

  @Test
  void test1() {
    int n = 1001; // 테스트할 유저 수

    User blocker = userRepository.save(UserFixture.createTestUser(1));
    List<User> blockedUsers = makeUsers(n);

    // 워밍업 (JIT·커넥션 준비)
    blockBulkRepository.bulkInsert(blocker, blockedUsers);
    jdbcTemplate.update("DELETE FROM block");

    // 1) JDBC 배치
    long tJdbc = measure(() -> blockBulkRepository.bulkInsert(blocker, blockedUsers));
    long countJdbc = countBlocks();
    jdbcTemplate.update("DELETE FROM block");

    // 2) JPA 배치
    long tJpa = measure(() -> {
      List<Block> blocks = toBlocks(blocker, blockedUsers);
      blockRepository.saveAll(blocks);
      blockRepository.flush();
    });
    long countJpa = countBlocks();
    jdbcTemplate.update("DELETE FROM block");

    System.out.printf("[DataJpaTest/MySQL][N=%d] JDBC=%d ms, JPA=%d ms (rows jdbc=%d, jpa=%d)%n",
                      n, tJdbc, tJpa, countJdbc, countJpa);
  }

  private List<User> makeUsers(final int n) {
    List<User> users = IntStream.rangeClosed(2, n)
                                .mapToObj(UserFixture::createTestUser)
                                .toList();
    return userRepository.saveAll(users);
  }

  private static long measure(Runnable r) {
    long t0 = System.nanoTime();
    r.run();
    return Duration.ofNanos(System.nanoTime() - t0).toMillis();
  }
  private long countBlocks() {
    return jdbcTemplate.queryForObject("select count(*) from block", Long.class);
  }
  private static List<Block> toBlocks(User blocker, List<User> blockedList) {
    List<Block> blocks = new ArrayList<>(blockedList.size());
    for (User u : blockedList) {
      Block build = Block.builder().blocker(blocker).blockedUser(u).build();
      blocks.add(build);
    }
    return blocks;
  }

}
