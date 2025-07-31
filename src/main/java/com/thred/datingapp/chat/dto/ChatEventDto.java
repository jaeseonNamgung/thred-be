package com.thred.datingapp.chat.dto;

public record ChatEventDto(
    String eventType,
    Long userId,
    Long chatRoomId
) {
  public static ChatEventDto chatConnectEvent(Long userId, Long chatRoomId) {
    return new ChatEventDto("connect", userId, chatRoomId);
  }

  public static ChatEventDto chatDisconnectEvent(Long userId, Long chatRoomId) {
    return new ChatEventDto("disconnect", userId, chatRoomId);
  }
}
