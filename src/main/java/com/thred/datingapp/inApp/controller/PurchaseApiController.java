package com.thred.datingapp.inApp.controller;

import com.thred.datingapp.common.api.response.ApiDataResponse;
import com.thred.datingapp.common.api.response.PageResponse;
import com.thred.datingapp.inApp.Service.PurchaseService;
import com.thred.datingapp.inApp.dto.request.ReceiptRequest;
import com.thred.datingapp.inApp.dto.request.ThreadRequest;
import com.thred.datingapp.inApp.dto.response.ProductResponse;
import com.thred.datingapp.inApp.dto.response.ThreadUseHistoryResponse;
import com.thred.datingapp.user.argumentResolver.Login;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/api/purchase")
@RequiredArgsConstructor
@RestController
public class PurchaseApiController {

    private final PurchaseService purchaseService;

    @PostMapping("/verify/receipt")
    public ApiDataResponse<Boolean> verifyReceipt(
            @RequestBody @Valid ReceiptRequest receiptRequest,
            @Login Long userId
            ) {

        log.debug("[verifyReceipt] userId = {}", userId);
        log.debug("[verifyReceipt] receiptRequest = {}", receiptRequest);
        return ApiDataResponse.ok(purchaseService.processInAppPurchase(userId, receiptRequest));
    }

    @PostMapping("/use/thread")
    public ApiDataResponse<Boolean> useThread(@Login Long userId, @RequestBody ThreadRequest threadRequest) {
        log.debug("[useThread] userId = {}", userId);
        log.debug("[useThread] threadRequest = {}", threadRequest);
        return ApiDataResponse.ok(purchaseService.useThread(userId, threadRequest));
    }

    @GetMapping("/use/thread/history/all")
    public ApiDataResponse<PageResponse<ThreadUseHistoryResponse>> getThreadUseHistory(
        @Login Long userId,
        @RequestParam("pageLastId")Long pageLastId,
        @RequestParam(required = false, name = "pageSize") int pageSize
    ) {

        log.debug("[getThreadUseHistory] userId = {}", userId);
        log.debug("[getThreadUseHistory] pageLastId = {}", pageLastId);
        log.debug("[getThreadUseHistory] pageSize = {}", pageSize);
        return ApiDataResponse.ok(purchaseService.getAllThreadUseHistories(userId, pageLastId, pageSize));
    }

    @GetMapping("/product/all")
    public ApiDataResponse<List<ProductResponse>> getAllProduct(){
        return ApiDataResponse.ok(purchaseService.getAllProducts());
    }

    @GetMapping("/thread")
    public ApiDataResponse<Integer> getTotalThread(@Login Long userId) {
        log.debug("[getTotalThread] userId = {}", userId);
        return ApiDataResponse.ok(purchaseService.getTotalThread(userId));
    }

    @GetMapping("/history/{purchaseTargetUserId}")
    public ApiDataResponse<Boolean> existsThreadUseHistory(
            @Login Long userId,
            @PathVariable("purchaseTargetUserId") Long purchaseTargetUserId,
            @RequestParam("purchaseType") String purchaseType
    ) {
        log.debug("[existsThreadUseHistory] userId = {}", userId);
        log.debug("[existsThreadUseHistory] purchaseTargetUserId = {}", purchaseTargetUserId);
        log.debug("[existsThreadUseHistory] purchaseType = {}", purchaseType);
        return ApiDataResponse.ok(purchaseService.existsThreadUseHistory(userId, purchaseTargetUserId, purchaseType));
    }


}
