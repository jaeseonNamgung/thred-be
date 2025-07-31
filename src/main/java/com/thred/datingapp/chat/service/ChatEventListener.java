package com.thred.datingapp.chat.service;

import com.thred.datingapp.chat.dto.ChatEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatEventListener {

  private final ChatService     chatService;
  private final ChatRoomService chatRoomService;

  @EventListener
  public void handleChatConnect(final ChatEventDto chatEventDto) {
    Long chatRoomId = chatEventDto.chatRoomId();
    Long userId = chatEventDto.userId();
    if ("disconnect".equals(chatEventDto.eventType())) {
      chatRoomService.disconnectFromChatRoom(chatRoomId, userId);
      log.debug("[handleChatConnect] User disconnected from chat room ===> chatRoomId: {}, userId: {}", chatRoomId, userId);
      return;
    }
    // 채팅방 ID + 회원 ID 레디스에 저장
    chatRoomService.saveChatConnectionInfo(chatRoomId, userId);
    log.debug("[handleChatConnect] Saved chat connection info ===> chatRoomId: {}, userId: {}", chatRoomId, userId);

    // 읽기 상태 처리
    chatService.updateReadStatus(chatRoomId, userId);
    log.debug("[handleChatConnect] Updated read status ===> chatRoomId: {}, userId: {}", chatRoomId, userId);

    // 상대방에게 접속 알림 전송
    chatService.notifyUserChatRoomEvent(chatRoomId, userId, chatEventDto.eventType());
    log.debug("[handleChatConnect] Notified user chat event ===> chatRoomId: {}, userId: {}, eventType: {}", chatRoomId, userId,
              chatEventDto.eventType());
  }
}
