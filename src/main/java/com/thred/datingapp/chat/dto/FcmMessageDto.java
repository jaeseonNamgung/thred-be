package com.thred.datingapp.chat.dto;

public record FcmMessageDto(
        NotificationDto notificationDto,
        String token
) {
    public static FcmMessageDto of(NotificationDto notificationDto, String token) {
        return new FcmMessageDto(notificationDto, token);
    }

    public static FcmMessageDto fromResponse(NotificationDto notificationDto, String fcmToken) {
        return FcmMessageDto.of(notificationDto, fcmToken);
    }
}
