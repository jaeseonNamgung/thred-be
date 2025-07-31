package com.thred.datingapp.inApp.Service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import com.thred.datingapp.common.config.GoogleCredentialsConfig;
import com.thred.datingapp.common.entity.inApp.Receipt;
import com.thred.datingapp.common.entity.inApp.type.ConsumptionState;
import com.thred.datingapp.common.entity.inApp.type.PurchaseState;
import com.thred.datingapp.common.entity.inApp.type.RevocationReason;
import com.thred.datingapp.common.entity.inApp.type.RevocationStatus;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.InAppErrorCode;
import com.thred.datingapp.inApp.dto.DeveloperNotification;
import com.thred.datingapp.inApp.dto.VerifiedProductDto;
import com.thred.datingapp.inApp.dto.VoidedPurchaseNotification;
import com.thred.datingapp.inApp.dto.request.GoogleRtdnRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoogleInAppService {

    private final GoogleCredentialsConfig credentialsConfig;
    private final ReceiptManagerService receiptManagerService;
    private final ObjectMapper objectMapper;

    @Value("${google.in-app.package-name}")
    private String packageName;

    public VerifiedProductDto verifyReceipt(final String productId, final String token) {

        try {
            // 1. Google API 호출 인증
            AndroidPublisher androidPublisher = credentialsConfig.androidPublisher();
            // 2. 구매 검증
            ProductPurchase productPurchase = verifyProduct(androidPublisher, productId, token);
            // 3. Google 서버에 구매 확정 요청
            androidPublisher.purchases().products().consume(packageName, productId, token).execute();
            return VerifiedProductDto.fromDto(productPurchase);
        } catch (IOException e) {
            log.error("[verifyReceipt] 영수즘 검증 에러입니다. ===> errorMessage: {}", e.getMessage());
            throw new CustomException(InAppErrorCode.APP_SERVER_ERROR, e);
        }
    }

    public void processNotification(final GoogleRtdnRequest googleRtdnRequest) {
        byte[] decodedData = Base64.getDecoder().decode(googleRtdnRequest.getMessage().getData());
        String decodedJson = new String(decodedData, StandardCharsets.UTF_8);
        try {
            DeveloperNotification developerNotification = objectMapper.readValue(decodedJson, DeveloperNotification.class);
            VoidedPurchaseNotification voidedPurchaseNotification = developerNotification.voidedPurchaseNotification();
            if( voidedPurchaseNotification == null) {
                log.error("[processNotification] voidedPurchaseNotification 객체가 null입니다. ===> developerNotification: {}", developerNotification);
                throw new CustomException(InAppErrorCode.GOOGLE_NOTIFICATION_ERROR);
            }
            handleRefund(developerNotification);
        } catch (JsonProcessingException e) {
            log.error("[processNotification] JSON 객체 변환 중 오류입니다. ===> decodedJson: {}, errorMessage: {}", decodedJson, e.getMessage());
            throw new CustomException(InAppErrorCode.GOOGLE_NOTIFICATION_ERROR, e);
        }
    }

    private ProductPurchase verifyProduct(final AndroidPublisher androidPublisher, final String productId, final String token) throws IOException {
        ProductPurchase productPurchase = androidPublisher
                .purchases()
                .products()
                .get(packageName, productId, token)
                .execute();

        PurchaseState purchaseState = PurchaseState.findType(productPurchase.getPurchaseState());
        ConsumptionState consumptionState = ConsumptionState.findType(productPurchase.getConsumptionState());

        // 상품이 취소 또는 환불 되었거나 상품이 이미 소비되었을 경우 예외 처리
        boolean invalidState = !PurchaseState.PURCHASED.equals(purchaseState) || ConsumptionState.CONSUMED.equals(consumptionState);
        if (invalidState) {
            log.error("[verifyReceipt] 상품이 취소 또는 환불 되었거나 상품이 이미 소비되었습니다. ===> purchaseState: {}, consumptionState: {}",
                    purchaseState, consumptionState);
            throw new CustomException(InAppErrorCode.ALREADY_PURCHASED_PRODUCT);
        }
        return productPurchase;
    }

    private void handleRefund(final DeveloperNotification developerNotification){
        VoidedPurchaseNotification voidedPurchaseNotification = developerNotification.voidedPurchaseNotification();
        String orderId = voidedPurchaseNotification.orderId();
        Receipt receipt = receiptManagerService.validationReceipt(orderId);

        RevocationReason revocationReason = RevocationReason.findType(voidedPurchaseNotification.refundType());
        Long eventTimeMillis = developerNotification.eventTimeMillis();
        receiptManagerService.updateRevocationStatus(revocationReason, eventTimeMillis, RevocationStatus.SUCCESS, receipt);
    }


}
