package com.thred.datingapp.inApp.controller;

import com.apple.itunes.storekit.model.ResponseBodyV2;
import com.thred.datingapp.common.api.response.ApiDataResponse;
import com.thred.datingapp.inApp.Service.AppleInAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/apple")
@RestController
public class AppleInAppApiController {

    private final AppleInAppService appleInAppService;

    @PostMapping("/notification/v2")
    public ApiDataResponse<Boolean> inAppNotification(@RequestBody ResponseBodyV2 responseBodyV2){
        log.info("[API CALL] /api/apple/notification/v2 - 애플 Notification 요청");
        log.debug("[inAppNotification] responseBodyV2 = {}", responseBodyV2);
        return ApiDataResponse.ok(appleInAppService.processAppleInAppNotification(responseBodyV2.getSignedPayload()));
    }

}
