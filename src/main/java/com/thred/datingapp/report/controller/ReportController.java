package com.thred.datingapp.report.controller;

import com.thred.datingapp.common.api.response.ApiDataResponse;
import com.thred.datingapp.user.api.response.ProcessingResultResponse;
import com.thred.datingapp.user.argumentResolver.Login;
import com.thred.datingapp.report.dto.request.ReportRequest;
import com.thred.datingapp.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/report")
    public ApiDataResponse<ProcessingResultResponse> createReport(@Login Long userId,
                                                                  @RequestPart("report") @Valid ReportRequest report,
                                                                  @RequestPart("evidence") MultipartFile evidence) {

        return ApiDataResponse.ok(ProcessingResultResponse.from(reportService.createReport(userId, report, evidence)));
    }
}
