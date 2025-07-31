package com.thred.datingapp.inApp.dto;

import com.google.api.services.androidpublisher.model.ProductPurchase;
import com.thred.datingapp.common.entity.inApp.Product;
import com.thred.datingapp.common.entity.inApp.Receipt;
import com.thred.datingapp.common.entity.inApp.UserAsset;
import com.thred.datingapp.common.entity.inApp.type.InAppType;
import com.thred.datingapp.common.entity.inApp.type.RevocationReason;
import com.thred.datingapp.common.entity.inApp.type.TransactionType;
import com.thred.datingapp.common.entity.user.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record VerifiedProductDto(
        String productId,
        String transactionId,
        String originalTransactionId,
        InAppType inAppType,
        TransactionType transactionReason,
        LocalDateTime purchaseDate,
        RevocationReason revocationReason,
        LocalDateTime revocationDate

) {

    public static VerifiedProductDto of(
            String productId,
            String transactionId,
            String originalTransactionId,
            InAppType inAppType,
            TransactionType transactionReason,
            LocalDateTime purchaseDate,
            RevocationReason revocationReason,
            LocalDateTime revocationDate
    ){
        return new VerifiedProductDto(
                productId,
                transactionId,
                originalTransactionId,
                inAppType,
                transactionReason,
                purchaseDate,
                revocationReason,
                revocationDate );
    }
    public static VerifiedProductDto fromDto(
            String productId,
            String transactionId,
            String originalTransactionId,
            InAppType inAppType,
            TransactionType transactionReason,
            LocalDateTime purchaseDate,
            RevocationReason revocationReason,
            LocalDateTime revocationDate

            ) {
        return VerifiedProductDto.of(
                productId,
                transactionId,
                originalTransactionId,
                inAppType,
                transactionReason,
                purchaseDate,
                revocationReason,
                revocationDate
        );
    }

    public static VerifiedProductDto fromDto(ProductPurchase productPurchase) {
        return VerifiedProductDto.of(
                productPurchase.getProductId(),
                productPurchase.getOrderId(),
                null,
                InAppType.GOOGLE,
                TransactionType.PURCHASE,
                convertMillisToLocalDateTime(productPurchase.getPurchaseTimeMillis()),
                null,
                null);
    }

    public Receipt toReceiptEntity(User user, Product product) {
        return Receipt.builder()
                .transactionId(transactionId)
                .originalTransactionId(originalTransactionId)
                .inAppType(inAppType)
                .transactionReason(transactionReason)
                .purchaseDate(purchaseDate)
                .revocationReason(revocationReason)
                .revocationDate(revocationDate)
                .user(user)
                .product(product)
                .build();
    }

    public UserAsset toUserAssetEntity(User user, Product product) {
        return UserAsset.builder()
                .totalThread(product.getQuantityThread())
                .user(user)
                .build();
    }

    private static LocalDateTime convertMillisToLocalDateTime(Long purchaseTimeMillis) {
        Instant instant = Instant.ofEpochMilli(purchaseTimeMillis);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
