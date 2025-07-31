package com.thred.datingapp.inApp.dto;

public record DeveloperNotification(
        Long eventTimeMillis,
        VoidedPurchaseNotification voidedPurchaseNotification
) {
}
