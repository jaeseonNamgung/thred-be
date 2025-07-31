package com.thred.datingapp.common.entity.inApp;

import com.thred.datingapp.common.entity.BaseEntity;
import com.thred.datingapp.common.entity.inApp.type.InAppType;
import com.thred.datingapp.common.entity.inApp.type.RevocationReason;
import com.thred.datingapp.common.entity.inApp.type.RevocationStatus;
import com.thred.datingapp.common.entity.inApp.type.TransactionType;
import com.thred.datingapp.common.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Receipt extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transactionId; // 거래 마다 생성되는 ID (구글: orderId)
    @Column(nullable = false)
    private String originalTransactionId; // 최초 거래 성공 시 생성되는 ID, (구글: purchaseToken)
    @Column(nullable = false)
    private InAppType inAppType; // 인앱 타입(APPLE, GOOGLE)
    @Column(nullable = false)
    private TransactionType transactionReason; // 거래 종류(소모성, 구독)
    @Column(nullable = false)
    private LocalDateTime purchaseDate; // 구매 날짜, 구글: purchaseTimeMillis
    private RevocationReason revocationReason; // 환불 사유, 구글: purchaseState = 1
    private LocalDateTime revocationDate; // 환불 사유, 구글: refundTimeMillis
    private RevocationStatus revocationStatus; // 환불 상태
    private int revokedThreadCount;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userAsset_id")
    private UserAsset userAsset;

    @Builder
    public Receipt(
            String transactionId,
            String originalTransactionId,
            InAppType inAppType,
            TransactionType transactionReason,
            LocalDateTime purchaseDate,
            RevocationReason revocationReason,
            LocalDateTime revocationDate,
            int revokedThreadCount,
            User user,
            Product product,
            UserAsset userAsset) {
        this.transactionId = transactionId;
        this.originalTransactionId = originalTransactionId;
        this.inAppType = inAppType;
        this.transactionReason = transactionReason;
        this.purchaseDate = purchaseDate;
        this.revocationReason = revocationReason;
        this.revocationDate = revocationDate;
        this.revokedThreadCount = revokedThreadCount;
        this.user = user;
        this.product = product;
        this.userAsset = userAsset;
    }

    public void updateRevocationStatus(
            RevocationReason revocationReason,
            LocalDateTime revocationDate,
            RevocationStatus revocationStatus
    ) {
        this.revocationReason = revocationReason;
        this.revocationDate = revocationDate;
        this.revocationStatus = revocationStatus;
    }

    public void updateRevokedThreadCount(int remainingThreadCount, int productThreadCount) {
        if(remainingThreadCount < productThreadCount) {
            this.revokedThreadCount = remainingThreadCount;
        }else {
            this.revokedThreadCount = productThreadCount;
        }
    }
}
