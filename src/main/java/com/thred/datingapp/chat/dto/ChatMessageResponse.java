package com.thred.datingapp.chat.dto;

import com.thred.datingapp.common.entity.chat.Chat;
import com.thred.datingapp.common.entity.user.User;
import org.joda.time.LocalDateTime;

import java.io.Serializable;

public record ChatMessageResponse(
        Long chatId,
        Long chatRoomId,
        Long senderId,
        String senderNickName,
        String senderEmail,
        String senderMainProfile,
        String message,
        String createdMessageDate,
        boolean readStatus,
        boolean isPartnerLeftChatRoom
) implements Serializable {

    public static ChatMessageResponse of(
            Long chatId,
            Long chatRoomId,
            Long senderId,
            String senderNickName,
            String senderEmail,
            String senderMainProfile,
            String message,
            String createdMessageDate,
            boolean readStatus,
            boolean isPartnerLeftChatRoom
            ) {
        return new ChatMessageResponse(
                chatId,
                chatRoomId,
                senderId,
                senderNickName,
                senderEmail,
                senderMainProfile,
                message,
                createdMessageDate,
                readStatus,
                isPartnerLeftChatRoom);
    }

    public static ChatMessageResponse fromDto(
            Chat chat,
            User sender,
            Long chatRoomId
    ) {
        return ChatMessageResponse.of(
                chat.getId(),
                chatRoomId,
                sender.getId(),
                sender.getUsername(),
                sender.getEmail(),
                sender.getMainProfile(),
                chat.getMessage(),
                chat.getCreatedDate().toString(),
                chat.isReadStatus(),
                false
        );
    }
    public static ChatMessageResponse leaveChatRoom(
            User sender,
            Long chatRoomId
    ) {
        return ChatMessageResponse.of(
                0L,
                chatRoomId,
                sender.getId(),
                sender.getUsername(),
                sender.getEmail(),
                sender.getMainProfile(),
                sender.getUsername()+ "님이 채팅방을 나갔습니다.",
                LocalDateTime.now().toString(),
                false,
                true
        );
    }
    public static ChatMessageResponse enterChatRoom(
        User sender,
        Long chatRoomId
    ) {
        return ChatMessageResponse.of(
            0L,
            chatRoomId,
            sender.getId(),
            sender.getUsername(),
            sender.getEmail(),
            sender.getMainProfile(),
            sender.getUsername()+ "님이 입장했습니다.",
            LocalDateTime.now().toString(),
            true,
            false
        );
    }
}
