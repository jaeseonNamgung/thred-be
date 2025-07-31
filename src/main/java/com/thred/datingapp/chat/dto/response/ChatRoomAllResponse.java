package com.thred.datingapp.chat.dto.response;

import java.time.LocalDateTime;

public record ChatRoomAllResponse(
        Long chatRoomId,
        Long receiverId,
        String receiverNickName,
        String receiverMainProfile,
        String message,
        // 상대방 톡을 읽지 않은 수
        int unReadCount,
        String lastMessageDate
) {

    public static ChatRoomAllResponse from(
            Long chatRoomId,
            Long receiverId,
            String receiverNickName,
            String receiverMainProfile,
            String message,
            int unreadReadCount,
            LocalDateTime lastMessageDate) {
        return new ChatRoomAllResponse(
                chatRoomId,
                receiverId,
                receiverNickName,
                receiverMainProfile,
                message,
                unreadReadCount,
                lastMessageDate == null ? "" : lastMessageDate.toString()
        );
    }

}
