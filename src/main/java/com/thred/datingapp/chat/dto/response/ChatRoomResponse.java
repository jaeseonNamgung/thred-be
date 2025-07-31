package com.thred.datingapp.chat.dto.response;

import com.thred.datingapp.common.entity.chat.ChatRoom;

public record ChatRoomResponse (
        Long chatRoomId
) {
    public static ChatRoomResponse fromResponse(ChatRoom chatRoom) {
        return new ChatRoomResponse(chatRoom.getId());
    }

    public static ChatRoomResponse empty() {
        return new ChatRoomResponse(null);
    }
}
