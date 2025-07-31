package com.thred.datingapp.inApp.dto;

public record VoidedPurchaseNotification(
        String purchaseToken,
        String orderId,
        int productType,
        int refundType
) {
}
