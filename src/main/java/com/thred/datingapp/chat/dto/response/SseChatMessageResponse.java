package com.thred.datingapp.chat.dto.response;

import com.thred.datingapp.common.entity.chat.Chat;
import com.thred.datingapp.common.entity.chat.ChatRoom;
import com.thred.datingapp.common.entity.user.User;

import java.time.LocalDateTime;

public record SseChatMessageResponse(
        Long chatRoomId,
        Long receiverId,
        String receiverNickName,
        String receiverMainProfile,
        String message,
        Long unReadCount,
        LocalDateTime sendMessageTime
) {
    public static SseChatMessageResponse of(Long chatRoomId,
                                            Long receiverId,
                                            String receiverNickName,
                                            String receiverMainProfile,
                                            String message,
                                            Long unReadCount,
                                            LocalDateTime sendMessageTime
    ) {
        return new SseChatMessageResponse(
                chatRoomId,
                receiverId,
                receiverNickName,
                receiverMainProfile,
                message, unReadCount,
                sendMessageTime
        );
    }

    public static SseChatMessageResponse fromResponse(
            ChatRoom chatRoom,
            Chat chat,
            User receiver,
            Long unReadCount
    ) {
        return SseChatMessageResponse.of(
                chatRoom.getId(),
                receiver.getId(),
                receiver.getUsername(),
                receiver.getMainProfile(),
                chat.getMessage(),
                unReadCount,
                chat.getCreatedDate()
        );
    }
}
