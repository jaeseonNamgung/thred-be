package com.thred.datingapp.chat.dto;

import com.thred.datingapp.common.type.NotificationType;
import com.thred.datingapp.common.entity.user.User;

import java.time.LocalDateTime;

public record NotificationDto(
        NotificationType notificationType,
        Long userId,
        String nickName,
        String profile,
        String message,
        LocalDateTime sendMessageTime
) {
    public static NotificationDto of(NotificationType notificationType,Long userId, String nickName, String profile, String message, LocalDateTime sendMessageTime) {
        return new NotificationDto(notificationType,userId, nickName, profile, message, sendMessageTime);
    }

    public static NotificationDto fromResponse(User receiver){
        return NotificationDto.of(
            NotificationType.CHATROOM_CREATED,
            receiver.getId(),
            receiver.getUsername(),
            receiver.getMainProfile(),
            null,
            LocalDateTime.now()
        );
    }
    public static NotificationDto fromResponse(Long receiverId, String receiverUsername, String receiverMainProfile, String message, LocalDateTime sendMessageTime) {
        return NotificationDto.of(
            NotificationType.CHATROOM_CREATED,
            receiverId,
            receiverUsername,
            receiverMainProfile,
            message,
            sendMessageTime
        );
    }
}
