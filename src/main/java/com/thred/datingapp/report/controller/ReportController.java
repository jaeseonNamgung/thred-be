package com.thred.datingapp.report.controller;

import com.thred.datingapp.common.api.response.ApiDataResponse;
import com.thred.datingapp.user.api.response.ProcessingResultResponse;
import com.thred.datingapp.user.argumentResolver.Login;
import com.thred.datingapp.report.dto.request.ReportRequest;
import com.thred.datingapp.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/report")
    public ApiDataResponse<ProcessingResultResponse> createReport(@Login Long userId,
                                                                  @RequestPart("report") @Valid ReportRequest report,
                                                                  @RequestPart("evidence") MultipartFile evidence) {

        log.info("[API CALL] /api/report - 신고 요청");
        log.debug("[createReport] report = {}", report);
        log.debug("[createReport] evidence = {}", evidence);
        return ApiDataResponse.ok(ProcessingResultResponse.from(reportService.createReport(userId, report, evidence)));
    }
}
