package com.thred.datingapp.chat.service;

import com.thred.datingapp.chat.dto.ChatMessageResponse;
import com.thred.datingapp.common.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChatMessageSender {

  private final SimpMessagingTemplate messagingTemplate;

  public void notifyUserConnected(Long targetUserId, User sender, Long chatRoomId) {
    messagingTemplate.convertAndSendToUser(
        targetUserId.toString(),
        "/queue/chat/connected",
        ChatMessageResponse.enterChatRoom(sender, chatRoomId)
    );
  }
}
