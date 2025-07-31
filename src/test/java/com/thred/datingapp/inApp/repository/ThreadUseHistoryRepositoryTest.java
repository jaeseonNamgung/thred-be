package com.thred.datingapp.inApp.repository;

import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.inApp.dto.response.ThreadUseHistoryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@Sql({"classpath:db/schema.sql", "classpath:db/data.sql"})
@Import({P6SpyConfig.class, JpaConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class ThreadUseHistoryRepositoryTest {

  @Autowired
  private ThreadUseHistoryRepository threadUseHistoryRepository;

  @Test
  @DisplayName("[QueryDsl] 실 사용 이력 조회 페이징 테스트 - 유저 1 첫 페이징 처리")
  void findAllByUserIdTestCase() {
    // given

    // when
    Page<ThreadUseHistoryResponse> expectedPage = threadUseHistoryRepository.findHistoryAllByUserIdWithPaging(1L, 0L, 5);
    // then
    assertThat(expectedPage.isFirst()).isTrue();
    assertThat(expectedPage.getSize()).isEqualTo(5);
    assertThat(expectedPage.getContent().get(0).getThreadUserHistoryId()).isEqualTo(13);
    assertThat(expectedPage.getContent().get(1).getThreadUserHistoryId()).isEqualTo(12);
    assertThat(expectedPage.getContent().get(2).getThreadUserHistoryId()).isEqualTo(11);
    assertThat(expectedPage.getContent().get(3).getThreadUserHistoryId()).isEqualTo(10);
    assertThat(expectedPage.getContent().get(4).getThreadUserHistoryId()).isEqualTo(9);

  }

  @Test
  @DisplayName("[QueryDsl] 실 사용 이력 조회 페이징 테스트 - 유저 1 threadUserHistoryId 6 이후 부터 페이징 처리")
  void findAllByUserIdTestCase2() {
    // given

    // when
    Page<ThreadUseHistoryResponse> expectedPage = threadUseHistoryRepository.findHistoryAllByUserIdWithPaging(1L, 6L, 5);
    // then
    assertThat(expectedPage.isFirst()).isTrue();
    assertThat(expectedPage.getSize()).isEqualTo(5);
    assertThat(expectedPage.getContent().get(0).getThreadUserHistoryId()).isEqualTo(5);
    assertThat(expectedPage.getContent().get(1).getThreadUserHistoryId()).isEqualTo(4);
    assertThat(expectedPage.getContent().get(2).getThreadUserHistoryId()).isEqualTo(2);
    assertThat(expectedPage.getContent().get(3).getThreadUserHistoryId()).isEqualTo(1);

  }

}
