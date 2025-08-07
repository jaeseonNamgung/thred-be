package com.thred.datingapp.admin.controller;

import com.thred.datingapp.admin.dto.request.ReportResultRequest;
import com.thred.datingapp.admin.dto.request.ReviewStatusRequest;
import com.thred.datingapp.admin.dto.response.ReviewResponse;
import com.thred.datingapp.admin.service.ReviewService;
import com.thred.datingapp.common.api.response.ApiDataResponse;
import com.thred.datingapp.common.api.response.PageResponse;
import com.thred.datingapp.main.dto.response.UserDetailsResponse;
import com.thred.datingapp.report.dto.response.ReportContent;
import com.thred.datingapp.report.dto.response.ReportResponse;
import com.thred.datingapp.report.service.ReportService;
import com.thred.datingapp.user.api.response.ProcessingResultResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final ReviewService reviewService;
    private final ReportService reportService;

    @GetMapping("/review")
    public ApiDataResponse<PageResponse<ReviewResponse>> getReview(
        @RequestParam("type") String reviewStatusStr,
        @RequestParam(name = "pageLastId") Long pageLastId,
        @RequestParam("pageSize") int pageSize
    ) {
        log.info("[API CALL] /api/admin/review - 회원 심사 내역 전체 조회 요청");
        log.debug("[getReview] reviewStatus = {}", reviewStatusStr);
        log.debug("[getReview] pageLastId: {}", pageLastId);
        log.debug("[getReview] pageSize: {}", pageSize);
        return ApiDataResponse.ok(reviewService.getReview(reviewStatusStr, pageLastId, pageSize));
    }

    @GetMapping("/review/{reviewId}")
    public ApiDataResponse<UserDetailsResponse> getUserProfile(@PathVariable("reviewId") Long reviewId) {
        log.info("[API CALL] /api/admin/review/{reviewId} - 특정 회원 심사 내역 조회 요청");
        log.debug("[getUserProfile] reviewId = {}", reviewId);
        return ApiDataResponse.ok(reviewService.getReviewUserInfo(reviewId));
    }

    @PostMapping("/review/{reviewId}")
    public ApiDataResponse<ProcessingResultResponse> reviewStatus(@PathVariable("reviewId") Long reviewId,
                                                                  @RequestBody ReviewStatusRequest request) {
        log.info("[API CALL] /api/admin/review/{reviewId} - 특정 회원 심사 내역 저장 요청");
        log.debug("[reviewStatus] reviewId = {}", reviewId);
        log.debug("[reviewStatus] reviewStatusRequest = {}", request);
        reviewService.updateReview(reviewId, request.status(), request.reason());
        return ApiDataResponse.ok(ProcessingResultResponse.from(true));
    }

    @GetMapping("/report")
    public ApiDataResponse<PageResponse<ReportResponse>> getReports(@RequestParam("type") String reportType,
                                                                    @RequestParam(name = "pageLastId") Long pageLastId,
                                                                    @RequestParam("pageSize") int pageSize) {

        log.info("[API CALL] /api/admin/report - 신고 내역 전체 조회 요청");
        log.debug("[getReports] reportType = {}", reportType);
        log.debug("[getReports] pageLastId: {}", pageLastId);
        log.debug("[getReports] pageSize: {}", pageSize);
        return ApiDataResponse.ok(reportService.findPendingReports(reportType, pageLastId, pageSize));
    }

    @GetMapping("/report/{reportedUserId}/{targetId}")
    public ApiDataResponse<ReportContent> getReport(
            @PathVariable("reportedUserId") Long reportedUserId,
            @PathVariable("targetId") Long targetId,
            @RequestParam("type") String reportType) {
        log.info("[API CALL] /api/admin/report/{reportedUserId}/{targetId} - 특정 회원 신고 내역 조회 요청");
        log.debug("[getReport] reportedUserId = {}", reportedUserId);
        log.debug("[getReport] targetId: {}", targetId);
        log.debug("[getReport] type: {}", reportType);
        return ApiDataResponse.ok(reportService.findReportContent(reportedUserId, targetId, reportType));
    }

    @PostMapping("/report/result/{reportId}")
    public ApiDataResponse<ProcessingResultResponse> reportResult(@PathVariable("reportId") Long id,
                                                                  @Valid @RequestBody ReportResultRequest request) {
        log.info("[API CALL] /api/admin/report/result/{reportId} - 신고 결과 Update 요청");
        log.debug("[getReport] reportId = {}", id);
        log.debug("[getReport] request = {}", request);
        reportService.saveReportResult(id, request);
        return ApiDataResponse.ok(ProcessingResultResponse.from(true));
    }

}
