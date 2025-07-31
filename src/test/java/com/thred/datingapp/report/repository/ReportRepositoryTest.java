package com.thred.datingapp.report.repository;

import com.testFixture.ReportFixture;
import com.testFixture.UserFixture;
import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.report.Report;
import com.thred.datingapp.common.entity.report.ReportType;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.field.UserState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Import({JpaConfig.class, P6SpyConfig.class})
@DataJpaTest
class ReportRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;
  @Autowired
  private ReportRepository  reportRepository;



  @Test
  void findByReportedUserIdAndTargetIdAndReportTypeWithPaging() {
    // given
    setUp(10);
    User reported = entityManager.find(User.class, 2L);
    // when
    Optional<Report> reportOptional =
        reportRepository.findByReportedUserIdAndTargetIdAndReportType(reported.getId(), 1L, ReportType.COMMUNITY);
    // then
    assertThat(reportOptional).isNotEmpty();
  }

  @Test
  void findAllByReportTypeFetchReportedUser() {
    // given
    setUp(1000);
    // when
    Page<Report> reportPage =
        reportRepository.findAllByReportTypeFetchReportedUserWithPaging(ReportType.COMMUNITY, 0L, 10);
    // then
    assertThat(reportPage.isLast()).isFalse();
    assertThat(reportPage.getSize()).isEqualTo(10);
  }

  @Test
  @DisplayName("")
  void existsReport() {

    // 존재하지 않을 때
    boolean isFalse = reportRepository.existsReport(1L, 2L, 1L, ReportType.COMMUNITY);
    assertThat(isFalse).isFalse();

    // 존재할 때
    setUp(1000);
    boolean isTrue = reportRepository.existsReport(1L, 2L, 1L, ReportType.COMMUNITY);
    assertThat(isTrue).isTrue();
  }

  @Test
  @DisplayName("")
  void updateUserState() {
    // given
    setUp(1000);
    Report report = reportRepository.findByIdFetchReportedUser(1L).get();
    // when
    System.out.println(report.getReportedUser().getUserState());
    report.getReportedUser().updateUserState(UserState.SUSPENDED);
    entityManager.flush();
    entityManager.clear();
    // then
    User user = entityManager.find(User.class, report.getReportedUser().getId());
    System.out.println(user.getUserState());
    assertThat(user.getUserState()).isEqualTo(UserState.SUSPENDED);
  }
  
  @Test
  @DisplayName("")
  void deleteAllByReporterOrReportedUser() {
    setUp(10);
    User user1 = entityManager.find(User.class, 1L);
    User user2 = entityManager.find(User.class, 2L);
    Report report = ReportFixture.createCommunityReport(11, user2, user1);
    reportRepository.save(report);
    entityManager.flush();
    entityManager.clear();

    Report report1 = entityManager.find(Report.class, 1L);
    Report report11 = entityManager.find(Report.class, 11L);

    assertThat(report1.getReporter().getId()).isEqualTo(user1.getId());
    assertThat(report11.getReportedUser().getId()).isEqualTo(user1.getId());
    entityManager.flush();
    entityManager.clear();
    reportRepository.deleteAllByReporterOrReportedUser(report1.getId());
    entityManager.flush();
    entityManager.clear();

    report1 = entityManager.find(Report.class, 1L);
    report11 = entityManager.find(Report.class, 11L);

    assertThat(report1).isNull();
    assertThat(report11).isNull();

  }

  private void setUp(int count) {
    for (int i = 1; i <= count; i++) {
      User reporter = entityManager.merge(UserFixture.createTestUser(i * 2 - 1));
      User reported = entityManager.merge(UserFixture.createTestUser(i * 2));
      Report report = ReportFixture.createCommunityReport(i, reporter, reported);
      reportRepository.save(report);
    }
    entityManager.flush();
    entityManager.clear();
  }



}
