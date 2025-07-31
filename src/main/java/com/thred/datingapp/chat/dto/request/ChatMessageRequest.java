package com.thred.datingapp.chat.dto.request;

import com.thred.datingapp.common.entity.chat.Chat;
import com.thred.datingapp.common.entity.chat.ChatPart;

public record ChatMessageRequest(
        Long receiverId,
        String message,
        boolean isLeftChatRoom
) {

    public static ChatMessageRequest of(Long receiverId, String message, boolean isLeftChatRoom) {
        return new ChatMessageRequest(receiverId, message, isLeftChatRoom);
    }

    public Chat toEntity(ChatPart chatPart, boolean readStatus) {
        return Chat.builder()
                .message(message)
                .chatPart(chatPart)
                .readStatus(readStatus)
                .build();
    }
}
