package com.thred.datingapp.report.service;

import com.thred.datingapp.admin.dto.request.ReportResultRequest;
import com.thred.datingapp.chat.service.ChatRoomService;
import com.thred.datingapp.common.api.response.PageResponse;
import com.thred.datingapp.common.entity.report.Report;
import com.thred.datingapp.common.entity.report.ReportHistory;
import com.thred.datingapp.common.entity.report.ReportResult;
import com.thred.datingapp.common.entity.report.ReportType;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.field.UserState;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ReportService {

  private final ReportRepository        reportRepository;
  private final ReportHistoryRepository reportHistoryRepository;
  private final UserRepository          userRepository;
  private final CommunityService        communityService;
  private final CommentService          commentService;
  private final ChatRoomService         chatRoomService;
  private final S3Utils                 s3Utils;

  @Transactional
  public boolean createReport(final Long reporterId, final ReportRequest reportRequest, final MultipartFile evidence) {

    User reporter = userRepository.findById(reporterId).orElseThrow(() -> {
      log.error("[createReport] 신고한 회원이 존재하지 않습니다. ==> reporterId: {}", reporterId);
      return new CustomException(ReportErrorCode.INVALID_REPORT_REQUEST);
    });
    User reportedUser = userRepository.findById(reportRequest.reportedUserId()).orElseThrow(() -> {
      log.error("[createReport] 신고 대상 회원이 존재하지 않습니다. ===> reportedUserId: {}", reportRequest.reportedUserId());
      return new CustomException(ReportErrorCode.INVALID_REPORT_REQUEST);
    });

    boolean hasSameReport = checkDuplicateReport(reporterId, reportRequest);

    // 이미 신고한 이력
    if (!hasSameReport) {
      return false;
    }

    String evidenceUrl = saveEvidence(evidence);

    Report report = createNewReport(reportRequest, reporter, reportedUser, evidenceUrl);

    reportRepository.save(report);

    // 신고 이력 저장
    reportHistoryRepository.findByReportedUserIdAndTargetId(reportRequest.reportedUserId(), reportRequest.targetId()).orElseGet(() -> {
      ReportHistory reportHistory = ReportHistory.builder()
                                                 .reportedUserId(reportedUser.getId())
                                                 .reportedUserName(reportedUser.getUsername())
                                                 .reportedUserEmail(reportedUser.getEmail())
                                                 .targetId(reportRequest.targetId())
                                                 .reason(report.getReason())
                                                 .reportType(report.getReportType())
                                                 .build();
      reportHistoryRepository.save(reportHistory);
      return reportHistory;
    });

    log.debug("[createReport] 정상적으로 신고처리가 완료되었습니다. ===> reporterId: {}, reportedUserId: {}", reporterId, reportedUser.getId());
    return true;
  }

  @Transactional
  public void saveReportResult(final Long reportId, final ReportResultRequest resultRequest) {
    Report report = findReportById(reportId);

    s3Utils.deleteS3Image(report.getEvidenceUrl());
    reportRepository.deleteByReportId(reportId);
    log.debug("[saveReportResult] 신고 이력 삭제 완료(s3, report 삭제) ===> reportId: {}", reportId);

    Long reportHistoryId =
        reportHistoryRepository.findHistoryIdByReportedUserIdAndTargetId(report.getReportedUser().getId(), report.getTargetId()).orElseThrow(() -> {
          log.error("[saveReportResult] 존재하지 않은 Report History ===> reportedUserId: {}, targetId: {}", report.getReportedUser().getId(),
                    report.getTargetId());
          return new CustomException(ReportErrorCode.INVALID_REPORT_REQUEST);
        });

    ReportResult reportResult;
    if (resultRequest.result()) {
      reportResult = ReportResult.APPROVED;
      handleReportTargetDeletion(report.getReportType(), report.getTargetId(), report.getReportedUser().getId());
      reportHistoryRepository.updateReportApprovalResult(reportResult, LocalDate.now().plusDays(resultRequest.suspensionDays()), reportHistoryId,
                                                         ReportType.COMPLETE);
      report.getReportedUser().updateUserState(UserState.SUSPENDED);
    } else {
      reportResult = ReportResult.REJECTED;
      reportHistoryRepository.updateReportRejectResult(reportResult, reportHistoryId);
    }
    log.info("[saveReportResult] 신고 처리 결과: {} ===> reportId: {}", reportResult, reportId);
  }

  private void handleReportTargetDeletion(final ReportType reportType, final Long targetId, final Long reportedId) {
    switch (reportType) {
      case COMMUNITY: {
        communityService.deleteCommunity(targetId, reportedId);
        log.debug("[saveReportResult] 게시글 삭제 완료 ===> targetId: {}, reportedUserId: {}", targetId, reportedId);
        break;
      }
      case CHAT: {
        chatRoomService.deleteAllChatHistory(targetId, reportedId);
        log.debug("[saveReportResult] 채팅 삭제 완료 ===> targetId: {}, reportedUserId: {}", targetId, reportedId);
        break;
      }
      case COMMENT: {
        commentService.deleteCommentByAdmin(targetId, reportedId);
        log.debug("[saveReportResult] 커뮤니티 댓글 삭제 완료 ===> targetId: {}, reportedUserId: {}", targetId, reportedId);
        break;
      }
    }
  }

  public PageResponse<ReportResponse> findPendingReports(final String reportType, final Long pageLastId, final int pageSize) {
    ReportType findReportType = ReportType.findType(reportType);

    if (findReportType.equals(ReportType.COMPLETE)) {
      Page<ReportHistory> reportHistoryPage = reportHistoryRepository.findAllWithPaging(pageLastId, pageSize);
      List<ReportResponse> reportHistoryResponses = reportHistoryPage.getContent()
                                                                     .stream()
                                                                     .map(reportHistory -> ReportResponse.of(reportHistory.getId(),
                                                                                                             reportHistory.getReportedUserId(),
                                                                                                             reportHistory.getTargetId(),
                                                                                                             reportHistory.getReportType().getType(),
                                                                                                             reportHistory.getReason().getReason()))
                                                                     .toList();
      return PageResponse.of(pageSize, reportHistoryPage.isLast(), reportHistoryResponses);
    }
    Page<Report> reportPage = reportRepository.findAllByReportTypeFetchReportedUserWithPaging(findReportType, pageLastId, pageSize);
    List<ReportResponse> reportResponses = reportPage.stream()
                                                     .map(report -> ReportResponse.of(report.getId(), report.getReportedUser().getId(),
                                                                                      report.getTargetId(), report.getReportType().getType(),
                                                                                      report.getReason().getReason()))
                                                     .toList();
    return PageResponse.of(pageSize, reportPage.isLast(), reportResponses);
  }

  public ReportContent findReportContent(final Long reportedUserId, final Long targetId, final String reportType) {

    Report report =
        reportRepository.findByReportedUserIdAndTargetIdAndReportType(reportedUserId, targetId, ReportType.findType(reportType)).orElseThrow(() -> {
          log.error("[findReportContent] 존재하지 않은 Report ===> reportedUserId: {}, targetId: {}", reportedUserId, targetId);
          return new CustomException(ReportErrorCode.NO_REPORT);
        });

    return new ReportContent(report.getId(), report.getReportedUser().getId(), report.getReportedUser().getUsername(), report.getTargetId(),
                             report.getReason().getReason(), report.getReportType().getType(), report.getEvidenceUrl(), report.getContent());
  }

  @Transactional
  public void deleteAllReportsForWithdrawnUser(final Long userId) {
    reportRepository.deleteAllByReporterOrReportedUser(userId);
    log.debug("[deleteReportsForWithdrawnUser] 회원 탈퇴 신고 내역 삭제 완료 ===> userId: {}", userId);
  }

  public int getTotalRemainingSuspensionDays(Long userId) {
    return reportHistoryRepository.findSuspendedDateByReportedUserId(userId)
                                  .stream()
                                  .filter(Objects::nonNull)
                                  .mapToInt(date -> (int) ChronoUnit.DAYS.between(LocalDate.now(), date))
                                  .filter(days -> days > 0)
                                  .sum();
  }

  private Report findReportById(final Long reportId) {
    return reportRepository.findByIdFetchReportedUser(reportId).orElseThrow(() -> {
      log.error("[findReportById] 해당 신고 내역이 존재하지 않습니다. ===> reportId: {}", reportId);
      return new CustomException(ReportErrorCode.INVALID_REPORT_ID);
    });
  }

  private String saveEvidence(final MultipartFile evidence) {
    if (evidence != null) {
      return s3Utils.saveImage(evidence);
    }
    return null;
  }

  private boolean checkDuplicateReport(final Long reporterId, final ReportRequest reportRequest) {
    boolean hasSameReport =
        reportRepository.existsReport(reporterId, reportRequest.reportedUserId(), reportRequest.targetId(), reportRequest.getReportType());
    if (hasSameReport) {
      log.error("[checkDuplicateReport] 이미 존재하는 신고입니다.");
      return false;
    }
    return true;
  }

  private Report createNewReport(final ReportRequest reportRequest, final User reporter, final User reportedUser, final String evidenceUrl) {
    return Report.builder()
                 .reportType(reportRequest.getReportType())
                 .reason(reportRequest.getReportReason())
                 .content(reportRequest.content())
                 .reporter(reporter)
                 .reportedUser(reportedUser)
                 .targetId(reportRequest.targetId())
                 .evidenceUrl(evidenceUrl)
                 .build();
  }

}
