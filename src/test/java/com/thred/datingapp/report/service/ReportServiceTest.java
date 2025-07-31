package com.thred.datingapp.report.service;

import com.testFixture.UserFixture;
import com.thred.datingapp.admin.dto.request.ReportResultRequest;
import com.thred.datingapp.chat.service.ChatRoomService;
import com.thred.datingapp.common.api.response.PageResponse;
import com.thred.datingapp.common.entity.report.*;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.ReportErrorCode;
import com.thred.datingapp.common.utils.S3Utils;
import com.thred.datingapp.community.service.CommentService;
import com.thred.datingapp.community.service.CommunityService;
import com.thred.datingapp.report.dto.request.ReportRequest;
import com.thred.datingapp.report.dto.response.ReportContent;
import com.thred.datingapp.report.dto.response.ReportResponse;
import com.thred.datingapp.report.repository.ReportHistoryRepository;
import com.thred.datingapp.report.repository.ReportRepository;
import com.thred.datingapp.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

  @Mock
  private UserRepository          userRepository;
  @Mock
  private S3Utils                 s3Utils;
  @Mock
  private ReportRepository        reportRepository;
  @Mock
  private ReportHistoryRepository reportHistoryRepository;
  @Mock
  private CommunityService        communityService;
  @Mock
  private CommentService          commentService;
  @Mock
  private ChatRoomService         chatRoomService;
  @InjectMocks
  private ReportService           reportService;

  @Test
  @DisplayName("Reporter 가 존재하지 않은 회원인 경우 예외가 발생한다.")
  void createReportException() {
    // given
    MockMultipartFile evidence = createEvidence();
    ReportRequest reportRequest = createReportRequest();

    given(userRepository.findById(anyLong())).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> reportService.createReport(100L, reportRequest, evidence)).isInstanceOf(CustomException.class)
                                                                                       .hasMessage(
                                                                                           ReportErrorCode.INVALID_REPORT_REQUEST.getMessage());

    then(userRepository).should().findById(anyLong());
  }

  @Test
  @DisplayName("ReportedUser 가 존재하지 않은 회원인 경우 예외가 발생한다.")
  void createReportException2() {
    // given
    MockMultipartFile evidence = createEvidence();
    ReportRequest reportRequest = createReportRequest();

    given(userRepository.findById(anyLong())).willReturn(Optional.of(UserFixture.createTestUser(1)));
    given(userRepository.findById(anyLong())).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> reportService.createReport(100L, reportRequest, evidence)).isInstanceOf(CustomException.class)
                                                                                       .hasMessage(
                                                                                           ReportErrorCode.INVALID_REPORT_REQUEST.getMessage());

    then(userRepository).should().findById(anyLong());
    then(userRepository).should().findById(anyLong());
  }

  @Test
  @DisplayName("이미 존재하는 신고일 경우에는 false를 리턴")
  void createReportExceptionWhenSameReport() {
    // given
    MockMultipartFile evidence = createEvidence();
    ReportRequest reportRequest = new ReportRequest(2L, "CHAT", "성적 발언", "A", 1L);

    given(userRepository.findById(anyLong())).willReturn(Optional.of(UserFixture.createTestUser(1)));
    given(userRepository.findById(anyLong())).willReturn(Optional.of(UserFixture.createTestUser(2)));
    given(reportRepository.existsReport(anyLong(), anyLong(), anyLong(), any(ReportType.class))).willReturn(true);
    // when & then
    boolean isFalse = reportService.createReport(1L, reportRequest, evidence);

    assertThat(isFalse).isFalse();
    then(userRepository).should(times(2)).findById(anyLong());
    then(reportRepository).should().existsReport(anyLong(), anyLong(), anyLong(), any(ReportType.class));
  }

  @Test
  @DisplayName("정상적으로 신고를 생성한다. - history가 이미 존재할 경우 create 처리 안함")
  void createReportTest() {
    // given
    MockMultipartFile evidence = createEvidence();
    ReportRequest reportRequest = new ReportRequest(2L, "CHAT", "ABUSE", "A", 1L);
    ReportHistory reportHistory =
        ReportHistory.builder().reportedUserId(2L).reportedUserName("testName").reportedUserEmail("test@test.com").reason(ReportReason.ABUSE).build();

    given(userRepository.findById(anyLong())).willReturn(Optional.of(UserFixture.createTestUser(1)));
    given(userRepository.findById(anyLong())).willReturn(Optional.of(UserFixture.createTestUser(2)));
    given(reportRepository.existsReport(anyLong(), anyLong(), anyLong(), any(ReportType.class))).willReturn(false);
    given(reportHistoryRepository.findByReportedUserIdAndTargetId(anyLong(), anyLong())).willReturn(Optional.of(reportHistory));
    // when
    reportService.createReport(1L, reportRequest, evidence);
    // then
    then(userRepository).should(times(2)).findById(anyLong());
    then(reportRepository).should().existsReport(anyLong(), anyLong(), anyLong(), any(ReportType.class));
    then(s3Utils).should().saveImage(any(MultipartFile.class));
    then(reportRepository).should().save(any(Report.class));
    then(reportHistoryRepository).should().findByReportedUserIdAndTargetId(anyLong(), anyLong());
    then(reportHistoryRepository).should(never()).save(any(ReportHistory.class));
  }

  @Test
  @DisplayName("정상적으로 신고를 생성한다. - history 존재하지 않을 경우 create 처리")
  void createReportTest2() {
    // given
    MockMultipartFile evidence = createEvidence();
    ReportRequest reportRequest = new ReportRequest(2L, "CHAT", "ABUSE", "A", 1L);

    given(userRepository.findById(anyLong())).willReturn(Optional.of(UserFixture.createTestUser(1)));
    given(userRepository.findById(anyLong())).willReturn(Optional.of(UserFixture.createTestUser(2)));
    given(reportRepository.existsReport(anyLong(), anyLong(), anyLong(), any(ReportType.class))).willReturn(false);
    given(reportHistoryRepository.findByReportedUserIdAndTargetId(anyLong(), anyLong())).willReturn(Optional.empty());
    // when
    reportService.createReport(1L, reportRequest, evidence);
    // then
    then(userRepository).should(times(2)).findById(anyLong());
    then(reportRepository).should().existsReport(anyLong(), anyLong(), anyLong(), any(ReportType.class));
    then(s3Utils).should().saveImage(any(MultipartFile.class));
    then(reportRepository).should().save(any(Report.class));
    then(reportHistoryRepository).should().findByReportedUserIdAndTargetId(anyLong(), anyLong());
    then(reportHistoryRepository).should().save(any(ReportHistory.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {"chat", "COMMENT", "community"})
  @DisplayName("각 타입에 맞는 신고들을 조회한다.")
  void findPendingReportsTest(String type) {
    // given
    int pageSize = 10;
    Long pageLastId = 0L;
    Report report = createReport(type);
    given(reportRepository.findAllByReportTypeFetchReportedUserWithPaging(any(ReportType.class), anyLong(), anyInt())).willReturn(new PageImpl<>(List.of(report)));
    // when
    PageResponse<ReportResponse> pageResponse = reportService.findPendingReports(type, pageLastId, pageSize);
    // then
    assertThat(pageResponse.isLastPage()).isTrue();
    assertThat(pageResponse.contents()).isNotEmpty();
    then(reportRepository).should().findAllByReportTypeFetchReportedUserWithPaging(any(ReportType.class), anyLong(), anyInt());
  }

  @Test
  @DisplayName("Complete 타입일 경우 history를 조회한다.")
  void findPendingReportsTest2() {
    // given
    int pageSize = 10;
    Long pageLastId = 0L;
    ReportHistory reportHistory = createReportHistory();
    given(reportHistoryRepository.findAllWithPaging(anyLong(), anyInt())).willReturn(new PageImpl<>(List.of(reportHistory)));
    // when
    PageResponse<ReportResponse> pageResponse = reportService.findPendingReports("complete", pageLastId, pageSize);
    // then
    assertThat(pageResponse.isLastPage()).isTrue();
    assertThat(pageResponse.contents()).isNotEmpty();
    then(reportHistoryRepository).should().findAllWithPaging(anyLong(), anyInt());
  }

  @Test
  @DisplayName("신고 세부사항 조회 테스트")
  void findReportContent() {
    // given

    given(reportRepository.findByReportedUserIdAndTargetIdAndReportType(anyLong(), anyLong(), any(ReportType.class))).willReturn(
        Optional.of(createReport("community")));
    // when
    ReportContent expectedResponse = reportService.findReportContent(2L, 1L, "community");
    // then
    assertThat(expectedResponse).hasFieldOrPropertyWithValue("reportedUserName", "testuser2")
                                .hasFieldOrPropertyWithValue("reason", ReportReason.ABUSE.getReason())
                                .hasFieldOrPropertyWithValue("type", ReportType.findType("community").getType())
                                .hasFieldOrPropertyWithValue("evidenceUrl", "https://cdn.example.com/report/evidence1.png")
                                .hasFieldOrPropertyWithValue("content", "욕설을 반복적으로 사용함");
    then(reportRepository).should().findByReportedUserIdAndTargetIdAndReportType(anyLong(), anyLong(), any(ReportType.class));
  }

  @Test
  @DisplayName("신고 대상 이력이 없을 경우 예외 발생")
  void findReportContentException() {
    // given
    given(reportRepository.findByReportedUserIdAndTargetIdAndReportType(anyLong(), anyLong(), any(ReportType.class))).willReturn(Optional.empty());
    // when
    CustomException expectedException = assertThrows(CustomException.class, () -> reportService.findReportContent(1L, 1L, "community"));
    // then
    assertThat(expectedException).hasFieldOrPropertyWithValue("message", ReportErrorCode.NO_REPORT.getMessage());
    then(reportRepository).should().findByReportedUserIdAndTargetIdAndReportType(anyLong(), anyLong(), any(ReportType.class));
  }

  @Test
  @DisplayName("존재하지 않은 신고일 경우 예외가 발생한다.")
  void saveReportResultException() {
    // given
    ReportResultRequest request = new ReportResultRequest(true, 10);
    // when & then
    assertThatThrownBy(() -> reportService.saveReportResult(1L, request)).isInstanceOf(CustomException.class)
                                                                         .hasMessage(ReportErrorCode.INVALID_REPORT_ID.getMessage());
  }

  /*
   * 1. 존재하지 않은 ReportHistory일 경우 예외 처리
   * 2. approval일 경우 delete 처리
   * 3. reject일 경우 처리
   *
   * */

  @Test
  @DisplayName("ReportHistory가 존재하지 않을 경우 Exception 처리")
  void saveReportResult_ifReportHistoryNotExist_throwsException() {
    // given
    ReportResultRequest reportResultRequest = new ReportResultRequest(true, 10);
    given(reportRepository.findByIdFetchReportedUser(anyLong())).willReturn(Optional.of(createReport("community")));
    given(reportHistoryRepository.findHistoryIdByReportedUserIdAndTargetId(anyLong(), anyLong())).willReturn(Optional.empty());
    // when
    CustomException customException = assertThrows(CustomException.class, () -> reportService.saveReportResult(1L, reportResultRequest));
    // then
    assertThat(customException).hasFieldOrPropertyWithValue("errorCode", ReportErrorCode.INVALID_REPORT_REQUEST);

    then(reportRepository).should().findByIdFetchReportedUser(anyLong());
    then(s3Utils).should().deleteS3Image(anyString());
    then(reportRepository).should().deleteByReportId(anyLong());
    then(reportHistoryRepository).should().findHistoryIdByReportedUserIdAndTargetId(anyLong(), anyLong());
  }

  @ParameterizedTest
  @ValueSource(strings = {"community", "comment", "chat"})
  @DisplayName("ReportResult가 true일 때 Approval 처리 테스트")
  void givenReportResultRequest_whenResultIsTrue_thenProcessApproval(String type) {
    // given
    ReportResultRequest reportResultRequest = new ReportResultRequest(true, 10);


    given(reportRepository.findByIdFetchReportedUser(anyLong())).willReturn(Optional.of(createReport(type)));
    given(reportHistoryRepository.findHistoryIdByReportedUserIdAndTargetId(anyLong(), anyLong())).willReturn(Optional.of(1L));
    // when
    reportService.saveReportResult(1L, reportResultRequest);
    // then

    then(reportRepository).should().findByIdFetchReportedUser(anyLong());
    then(s3Utils).should().deleteS3Image(anyString());
    then(reportRepository).should().deleteByReportId(anyLong());
    then(reportHistoryRepository).should().findHistoryIdByReportedUserIdAndTargetId(anyLong(), anyLong());
    switch (type) {
      case "community" -> then(communityService).should().deleteCommunity(anyLong(), anyLong());
      case "comment" -> then(commentService).should().deleteCommentByAdmin(anyLong(), anyLong());
      case "chat" -> then(chatRoomService).should().deleteAllChatHistory(anyLong(), anyLong());
    }
    then(reportHistoryRepository).should()
                                 .updateReportApprovalResult(any(ReportResult.class), any(LocalDate.class), anyLong(), any(ReportType.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {"community", "comment", "chat"})
  @DisplayName("ReportResult가 false일 때 Reject 처리 테스트")
  void givenReportResultRequest_whenResultIsFalse_thenProcessReject(String type) {
    // given
    ReportResultRequest reportResultRequest = new ReportResultRequest(false, 10);

    given(reportRepository.findByIdFetchReportedUser(anyLong())).willReturn(Optional.of(createReport(type)));
    given(reportHistoryRepository.findHistoryIdByReportedUserIdAndTargetId(anyLong(), anyLong())).willReturn(Optional.of(1L));
    // when
    reportService.saveReportResult(1L, reportResultRequest);
    // then

    then(reportRepository).should().findByIdFetchReportedUser(anyLong());
    then(s3Utils).should().deleteS3Image(anyString());
    then(reportRepository).should().deleteByReportId(anyLong());
    then(reportHistoryRepository).should().findHistoryIdByReportedUserIdAndTargetId(anyLong(), anyLong());
    then(communityService).shouldHaveNoMoreInteractions();
    then(commentService).shouldHaveNoInteractions();
    then(chatRoomService).shouldHaveNoInteractions();
    then(userRepository).shouldHaveNoInteractions();
    then(reportHistoryRepository).should().updateReportRejectResult(any(ReportResult.class), anyLong());
  }

  private ReportHistory createReportHistory() {
    return ReportHistory.builder()
                        .reportedUserId(1L)
                        .reportedUserName("testReportedUser")
                        .reportedUserEmail("test@test.com")
                        .reason(ReportReason.ABUSE)
                        .reportType(ReportType.COMPLETE)
                        .targetId(1L)
                        .build();
  }

  private MockMultipartFile createEvidence() {
    return new MockMultipartFile("mainProfile", "profile.jpg", "image/jpeg", "Test file content" .getBytes());
  }

  private ReportRequest createReportRequest() {
    return new ReportRequest(200L, "COMMENT", "성적 비하", "A", 1L);
  }

  private Report createReport(String type) {
    User reporter = UserFixture.createTestUser(1);
    ReflectionTestUtils.setField(reporter, "id", 1L);
    User reportedUser = UserFixture.createTestUser(2);
    ReflectionTestUtils.setField(reportedUser, "id", 2L);
    return Report.builder()
                 .reportType(ReportType.findType(type))
                 .content("욕설을 반복적으로 사용함")
                 .reason(ReportReason.ABUSE)
                 .reporter(reporter)
                 .reportedUser(reportedUser)
                 .targetId(1L)
                 .evidenceUrl("https://cdn.example.com/report/evidence1.png")
                 .build();
  }

}
