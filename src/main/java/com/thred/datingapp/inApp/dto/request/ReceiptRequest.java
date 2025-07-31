package com.thred.datingapp.inApp.dto.request;

import com.thred.datingapp.common.entity.inApp.type.InAppType;

public record ReceiptRequest(
        String productId, // google
        String receiptData, // google: token
        InAppType inAppType
) {

    public static ReceiptRequest of(
            String productId,
            String receiptData,
            InAppType inAppType
    ){
        return new ReceiptRequest(productId, receiptData, inAppType);
    }
    public static ReceiptRequest of(
            String receiptData,
            InAppType inAppType
    ){
        return new ReceiptRequest(null, receiptData, inAppType);
    }

}
