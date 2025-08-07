package com.thred.datingapp.inApp.controller;

import com.thred.datingapp.inApp.Service.GoogleInAppService;
import com.thred.datingapp.inApp.dto.request.GoogleRtdnRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/google")
@RequiredArgsConstructor
@RestController
public class GoogleInAppApiController {

    private final GoogleInAppService googleInAppService;

    @PostMapping("/android/notification")
    public void receiptNotification(@RequestBody GoogleRtdnRequest googleRtdnRequest) {
        log.info("[API CALL] /api/android/notification - 구글 Notification 요청");
        log.debug("[receiptNotification] googleRtdnRequest = {}", googleRtdnRequest);
        googleInAppService.processNotification(googleRtdnRequest);
    }
}
