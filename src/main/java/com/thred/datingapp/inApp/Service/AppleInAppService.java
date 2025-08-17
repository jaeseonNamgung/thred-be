package com.thred.datingapp.inApp.Service;

import com.apple.itunes.storekit.client.APIException;
import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.migration.ReceiptUtility;
import com.apple.itunes.storekit.model.*;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import com.apple.itunes.storekit.verification.VerificationException;
import com.thred.datingapp.common.entity.inApp.Product;
import com.thred.datingapp.common.entity.inApp.Receipt;
import com.thred.datingapp.common.entity.inApp.UserAsset;
import com.thred.datingapp.common.entity.inApp.type.InAppType;
import com.thred.datingapp.common.entity.inApp.type.RevocationReason;
import com.thred.datingapp.common.entity.inApp.type.RevocationStatus;
import com.thred.datingapp.common.entity.inApp.type.TransactionType;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.InAppErrorCode;
import com.thred.datingapp.inApp.dto.VerifiedProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AppleInAppService {

    private final SignedDataVerifier signedDataVerifier = null;
    private final AppStoreServerAPIClient appStoreServerAPIClient = null;
    private final ReceiptManagerService receiptManagerService;

    public VerifiedProductDto verifyReceipt(final String receipt){

        try {
            // 1. 애플 영수증에서 TransactionId 추출
            String transactionId = extractTransactionIdFromReceipt(receipt);
            // 2. 단일 결제 건에 대한 정보 조회
            TransactionInfoResponse transactionInfo = appStoreServerAPIClient.getTransactionInfo(transactionId);
            JWSTransactionDecodedPayload decodedPayload =
                    signedDataVerifier.verifyAndDecodeTransaction(transactionInfo.getSignedTransactionInfo());
            // 3. 검증된 정보 매핑
            return mapToVerifiedProductDto(decodedPayload, transactionId);

        } catch (IOException e) {
            log.error("[verifyReceipt] 유효하지 않은 영수증 데이터 (Invalid app receipt data) ===> receipt: {}, errorMessage: {}", receipt, e.getMessage());
            throw new CustomException(InAppErrorCode.RECEIPT_NOT_VALID, e);
        } catch (APIException e) {
            log.error("[verifyReceipt] 애플 서버 오류 (Apple server error) ===> errorMessage: {}", e.getMessage());
            throw new CustomException(InAppErrorCode.APP_SERVER_ERROR, e);
        } catch (VerificationException e) {
            log.error("[verifyReceipt] 영수증 서명 검증 실패 (Receipt signature verification failed) ===> errorMessage: {}", e.getMessage());
            throw new CustomException(InAppErrorCode.RECEIPT_SIGNATURE_VERIFICATION_ERROR, e);
        }
    }


    @Transactional
    public boolean processAppleInAppNotification(final String signedPayload) {
        try {
            ResponseBodyV2DecodedPayload responseBodyV2DecodedPayload =
                    signedDataVerifier.verifyAndDecodeNotification(signedPayload);
            NotificationTypeV2 notificationType = responseBodyV2DecodedPayload.getNotificationType();
            switch (notificationType) {
                case CONSUMPTION_REQUEST -> handlerConsumptionRequest(responseBodyV2DecodedPayload);
                case REFUND -> processRefund(responseBodyV2DecodedPayload, RevocationStatus.SUCCESS);
                case REFUND_DECLINED -> processRefund(responseBodyV2DecodedPayload, RevocationStatus.FAILED);
            }
            return true;
        } catch (VerificationException e) {
            log.error("[processAppleInAppNotification] 서명 검증 실패 (Signature verification failed) ===> signedPayload: {}, errorMessage: {}", signedPayload, e.getMessage());
            throw new CustomException(InAppErrorCode.SIGNATURE_VERIFICATION_ERROR, e);
        }
    }

    private void processRefund(final ResponseBodyV2DecodedPayload responseBodyV2DecodedPayload, final RevocationStatus revocationStatus) throws VerificationException {

        // 1. transactionId 추출
        JWSTransactionDecodedPayload decodedPayload = extractTransactionIdFromPayload(responseBodyV2DecodedPayload);
        String transactionId = decodedPayload.getTransactionId();
        // 2. receipt, userAsset, product가 존재하는지 검증
        Receipt receipt = receiptManagerService.validationReceipt(transactionId);
        // 3. 환불 상태 처리 및 실타래 갯수 변경
        RevocationReason revocationReason = RevocationReason.findType(decodedPayload.getRevocationReason().getValue());
        Long revocationDate = decodedPayload.getRevocationDate();
        receiptManagerService.updateRevocationStatus(revocationReason, revocationDate, revocationStatus, receipt);
    }

    private void handlerConsumptionRequest(final ResponseBodyV2DecodedPayload responseBodyV2DecodedPayload) throws VerificationException {
        try {
            // 1. transactionId 추출
            JWSTransactionDecodedPayload decodedPayload = extractTransactionIdFromPayload(responseBodyV2DecodedPayload);

            // 2. receipt, userAsset, product가 존재하는지 검증
            Receipt receipt = receiptManagerService.validationReceipt(decodedPayload.getTransactionId());

            // 3. 환불 상태 처리 및 실타래 갯수 변경
            RevocationReason revocationReason = RevocationReason.findType(decodedPayload.getRevocationReason().getValue());
            Long revocationDate = decodedPayload.getRevocationDate();
            receiptManagerService.updateRevocationStatus(revocationReason, revocationDate, RevocationStatus.IN_PROGRESS, receipt);

            // 4. ConsumptionRequest 생성
            ConsumptionRequest request = createConsumptionRequest(receipt, decodedPayload.getAppAccountToken());
            appStoreServerAPIClient.sendConsumptionData(decodedPayload.getTransactionId(), request);

        } catch (APIException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("[handlerConsumptionRequest] 애플 인앱 API 요청 중 예외 발생 (Exception occurred while calling Apple In-App API) ===> errorMessage: {}", e.getMessage());
            throw new CustomException(InAppErrorCode.APP_SERVER_ERROR, e);
        }
    }

    private ConsumptionRequest createConsumptionRequest(final Receipt receipt, final UUID appAccountToken) {
        return new ConsumptionRequest()
                .accountTenure(AccountTenure.UNDECLARED) // 가입 기간
                .appAccountToken(appAccountToken)
                .consumptionStatus(getConsumptionStatus(receipt.getUserAsset(), receipt.getProduct())) // 소비 상태
                .customerConsented(true) // 고객 동의 여부 - 고객이 소비 데이터 제공에 동의했는지 여부
                // 상품 제공 여부 : product, userAsset 가 null 이 아니기 때문에 정상적으로 제공됨
                .deliveryStatus(DeliveryStatus.DELIVERED_AND_WORKING_PROPERLY)
                .lifetimeDollarsPurchased(LifetimeDollarsPurchased.UNDECLARED) // 고객 총 구매 금액
                .lifetimeDollarsRefunded(LifetimeDollarsRefunded.UNDECLARED)  // 고객 총 환불 금액
                .platform(Platform.APPLE) // 플랫폼
                .playTime(PlayTime.UNDECLARED) // 플랫폼 사용 시간
                .refundPreference(RefundPreference.PREFER_DECLINE) // 환불 선호도
                .sampleContentProvided(true) // 샘플 콘텐츠 제공 여부 - 구매 전 무료 체험판, 샘플 콘텐츠 또는 기능 정보를 제공했는지
                .userStatus(UserStatus.ACTIVE); // 사용자 상태
    }


    private static ConsumptionStatus getConsumptionStatus(final UserAsset userAsset, final Product product) {
        ConsumptionStatus consumptionStatus;
        if(userAsset.getTotalThread() == 0){
            consumptionStatus = ConsumptionStatus.FULLY_CONSUMED;
        }else if(userAsset.getTotalThread().equals(product.getQuantityThread())){
            consumptionStatus = ConsumptionStatus.NOT_CONSUMED;
        }else {
            consumptionStatus = ConsumptionStatus.PARTIALLY_CONSUMED;
        }
        return consumptionStatus;
    }

    private VerifiedProductDto mapToVerifiedProductDto(final JWSTransactionDecodedPayload decodedPayload, final String transactionId) {
        String productId = decodedPayload.getProductId();
        String originalTransactionId = decodedPayload.getOriginalTransactionId();
        TransactionType transactionReason = TransactionType.findType(decodedPayload.getTransactionReason().name());
        LocalDateTime purchaseDate =
                Instant.ofEpochMilli(decodedPayload.getPurchaseDate()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        RevocationReason revocationReason = RevocationReason.findType(decodedPayload.getRevocationReason().getValue());
        LocalDateTime revocationDate =
                Instant.ofEpochMilli(decodedPayload.getRevocationDate()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        return VerifiedProductDto.fromDto(
                productId,
                transactionId,
                originalTransactionId,
                InAppType.APPLE,
                transactionReason,
                purchaseDate,
                revocationReason,
                revocationDate);
    }

    private String extractTransactionIdFromReceipt(String receipt) throws IOException {
        ReceiptUtility receiptUtil = new ReceiptUtility();
        String transactionId = receiptUtil.extractTransactionIdFromAppReceipt(receipt);
        // 2. 단일 거래 정보 추출
        if(transactionId == null){
            log.error("[verifyReceipt] transactionId 값이 존재하지 않습니다. (transactionId is null) ===> receipt: {}", receipt);
            throw new CustomException(InAppErrorCode.RECEIPT_NOT_VALID);
        }
        return transactionId;
    }

    private JWSTransactionDecodedPayload extractTransactionIdFromPayload(ResponseBodyV2DecodedPayload decodedPayload) throws VerificationException {
        String signedTransactionInfo = decodedPayload.getData().getSignedTransactionInfo();
        return signedDataVerifier.verifyAndDecodeTransaction(signedTransactionInfo);
    }
}
