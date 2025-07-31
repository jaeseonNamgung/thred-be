package com.thred.datingapp.chat.dto.response;

public record UpdateReadStatusDto(
        Long chatRoomId,
        String email
) {
    public static UpdateReadStatusDto of(Long chatRoomId, String email) {
        return new UpdateReadStatusDto(chatRoomId, email);
    }
}
