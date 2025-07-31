package com.thred.datingapp.inApp.dto.request;

import com.thred.datingapp.common.entity.inApp.ThreadUseHistory;
import com.thred.datingapp.common.entity.inApp.type.PurchaseType;
import com.thred.datingapp.common.entity.user.User;

public record ThreadRequest(
        Long purchaseTargetUserId,
        String purchaseType
) {
    public ThreadUseHistory toEntity(User user) {
        PurchaseType foundPurchaseType = PurchaseType.findType(purchaseType);
        return ThreadUseHistory.builder()
                .purchaseTargetUserId(purchaseTargetUserId)
                .purchaseType(foundPurchaseType)
                .amount(foundPurchaseType.getAmount())
                .user(user)
                .build();
    }
}
