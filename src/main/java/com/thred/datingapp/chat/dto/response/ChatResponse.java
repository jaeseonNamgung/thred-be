package com.thred.datingapp.chat.dto.response;

import com.thred.datingapp.common.entity.chat.Chat;

public record ChatResponse(
        Long chatId,
        Long chatRoomId,
        Long senderId,
        String senderEmail,
        String senderNickName,
        String senderMainProfile,
        String message,
        String createdMessageDate,
        boolean readStatus
) {

    public static ChatResponse fromResponse(Chat chat) {
        return new ChatResponse(
                chat.getId(),
                chat.getChatPart().getChatRoom().getId(),
                chat.getChatPart().getUser().getId(),
                chat.getChatPart().getUser().getEmail(),
                chat.getChatPart().getUser().getUsername(),
                chat.getChatPart().getUser().getMainProfile(),
                chat.getMessage(),
                chat.getCreatedDate().toString(),
                chat.isReadStatus()
        );
    }

}
