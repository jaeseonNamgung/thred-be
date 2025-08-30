package com.thred.datingapp.inApp.Service;

import com.thred.datingapp.common.entity.inApp.Receipt;
import com.thred.datingapp.common.entity.inApp.type.RevocationReason;
import com.thred.datingapp.common.entity.inApp.type.RevocationStatus;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.InAppErrorCode;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.inApp.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;

    public Receipt validationReceipt(final String transactionId) {
        Receipt receipt = receiptRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> {
                    log.error("[handlerConsumptionRequest] 존재하지 않은 영수증입니다.(Not exist receipt) ===> transactionId: {}", transactionId);
                    return new CustomException(InAppErrorCode.NOT_EXIST_RECEIPT);
                });

        if(receipt.getUserAsset() == null) {
            log.error("[handlerConsumptionRequest] 회원 실 사용 이력이 존재하지 않습니다. (Not exist userAsset) ===> transactionId: {}", transactionId);
            throw new CustomException(InAppErrorCode.NOT_EXIST_USER_ASSET);
        }

        if(receipt.getProduct() == null){
            log.error("[handlerConsumptionRequest] 상품이 존재하지 않습니다. (Not exist product) ===> productId: {}", transactionId);
            throw new CustomException(InAppErrorCode.NOT_EXIST_PRODUCT);
        }
        return receipt;
    }

    public void updateRevocationStatus(final RevocationReason revocationReason, final Long revocationDate, final RevocationStatus revocationStatus, final Receipt receipt){

        receipt.updateRevocationStatus(
                revocationReason,
                Instant.ofEpochMilli(revocationDate).atZone(ZoneId.systemDefault()).toLocalDateTime(),
                revocationStatus);

        // 환불 상품 수량 처리
        switch (revocationStatus) {
            case IN_PROGRESS -> {
                int remainingThreadCount = receipt.getUserAsset().getTotalThread();
                int productThreadCount = receipt.getProduct().getQuantityThread();

                receipt.updateRevokedThreadCount(remainingThreadCount, productThreadCount);
                receipt.getUserAsset().quantityRemoveThread(productThreadCount);
            }
            case FAILED -> {
                receipt.updateRevokedThreadCount(0, 0);
                receipt.getUserAsset().quantityAddThread(receipt.getRevokedThreadCount());
            }
        }
    }

    @Transactional
    public void save(final Receipt receipt) {
        receiptRepository.save(receipt);
        log.debug("[save] Receipt 저장 완료 ===> receiptId: {}", receipt.getId());
    }

    @Transactional
    public void deleteByUserId(final Long userId) {
        if(userId == null) {
            log.error("[deleteByUserId] userId is Null");
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }
        receiptRepository.deleteByUserId(userId);
        log.debug("[deleteByUserId] Receipt 삭제 완료 ===> userId: {}", userId);
    }
}
