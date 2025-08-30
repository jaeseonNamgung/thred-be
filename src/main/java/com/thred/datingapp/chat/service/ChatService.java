package com.thred.datingapp.chat.service;

import com.thred.datingapp.chat.dto.request.ChatMessageRequest;
import com.thred.datingapp.chat.dto.response.ChatResponse;
import com.thred.datingapp.chat.dto.ChatMessageResponse;
import com.thred.datingapp.chat.dto.NotificationDto;
import com.thred.datingapp.chat.repository.ChatPartRepository;
import com.thred.datingapp.chat.repository.ChatRoomRepository;
import com.thred.datingapp.chat.repository.queryDsl.ChatRepository;
import com.thred.datingapp.common.api.response.PageResponse;
import com.thred.datingapp.common.entity.chat.Chat;
import com.thred.datingapp.common.entity.chat.ChatPart;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.errorCode.BaseErrorCode;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.common.service.NotificationService;
import com.thred.datingapp.common.utils.JwtUtils;
import com.thred.datingapp.common.utils.RedisUtils;
import com.thred.datingapp.user.properties.JwtProperties;
import com.thred.datingapp.user.service.UserService;
import io.jsonwebtoken.lang.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.thred.datingapp.chat.properties.ChatProperties.*;

@Slf4j
@Transactional(readOnly = true)
@Getter
@RequiredArgsConstructor
@Service
public class ChatService {

  private final ChatRepository        chatRepository;
  private final ChatRoomRepository    chatRoomRepository;
  private final ChatPartRepository    chatPartRepository;
  private final RedisUtils          redisUtil;
  private final NotificationService notificationService;
  private final JwtUtils            jwtUtils;
  private final SseEmitterService     sseEmitterService;
  private final SimpMessagingTemplate messagingTemplate;
  private final UserService           userService;

  @Transactional
  public void updateReadStatus(final Long chatRoomId, final Long userId) {
    //  접속한 사용자 라면 상대방 메시지를 읽음 처리
    ChatPart chatPart = chatPartRepository.findByChatRoomIdAndOtherConnectorId(chatRoomId, userId).orElseThrow(() -> {
      log.error("[updateReadStatus] 존재하지 않은 채팅 파트입니다. ===> chatRoomId: {}, userId: {}", chatRoomId, userId);
      return new CustomException(BaseErrorCode.INTERNAL_SERVER_ERROR);
    });
    log.debug("[updateReadStatus] chatPart 조회(Selected chatPart) ===> chatPart: {}", chatPart);
    // 유저 접속 -> 읽음 처리
    if (!chatPart.getChats().isEmpty()) {
      chatRepository.updateReadStatusByChatPartId(chatPart.getId());
      log.info("[updateReadStatus] 채팅 읽기 상태 변경(Updating read status) ===> chatPartId: {}", chatPart.getId());
    }
  }

  public void notifyUserChatRoomEvent(final Long chatRoomId, final Long userId, String eventType) {
    User senderId = userService.getUserById(userId);
    ChatMessageResponse chatMessageResponse;
    if(CHAT_CONNECT.equals(eventType)) {
      chatMessageResponse = ChatMessageResponse.enterChatRoom(senderId, chatRoomId);
    }else {
      chatMessageResponse = ChatMessageResponse.leaveChatRoom(senderId, chatRoomId);
    }
    // 상대방 실시간 알림 전송
    messagingTemplate.convertAndSend(CHAT_DESTINATION+chatRoomId, chatMessageResponse);
  }

  @Transactional
  public ChatMessageResponse sendMessage(final Long chatRoomId, final String accessToken, final ChatMessageRequest chatMessageRequest) {
    String rawAccessToken = Optional.ofNullable(jwtParse(accessToken)).orElseThrow(() -> {
      log.error("[sendMessage] Authorization 헤더 형식이 올바르지 않습니다. 'Bearer {token}' 형식이어야 합니다. ===> accessToken: {}", accessToken);
      return new CustomException(UserErrorCode.INVALID_TOKEN);
    });
    Long senderId = jwtUtils.getUserId(rawAccessToken);

    // 상대방이 접속 중 -> 읽음 처리
    User sender = userService.getUserById(senderId);
    log.debug("[sendMessage] sender 조회(Selected sender) ===> sender: {}", sender);

    // 채팅 중 채팅방을 나갈 경우 로직 처리
    if (chatMessageRequest.isLeftChatRoom()) {
      return ChatMessageResponse.leaveChatRoom(sender, chatRoomId);
    }

    Long receiverId = chatMessageRequest.receiverId();
    log.debug("[sendMessage] receiverId: {}", receiverId);

    Set<Long> connectedAllUser = isConnectedAllUser(chatRoomId);
    log.debug("[sendMessage] 접속된 사용자(Connected users) ===> connectedAllUser: {}", connectedAllUser);

    // 채팅방에 접속된 다른 사용자 조회
    Optional<Long> otherConnector = connectedAllUser.stream().filter(connector -> connector.equals(receiverId)).findFirst();
    log.debug("[sendMessage] 접속된 상대방(Connected recipient) ===>  otherConnector: {}", otherConnector);

    ChatPart chatPart = chatPartRepository.findByChatRoomIdAndUserId(chatRoomId, senderId)
                                          .orElseThrow(() -> new CustomException(BaseErrorCode.INTERNAL_SERVER_ERROR));
    log.debug("[sendMessage] chatPart 조회(Selected chatPart) ===>  chatPart: {}", chatPart);

    Chat savedChat;
    // 채팅방에 접속된 회원이 존재한다면
    if (otherConnector.isPresent()) {
      savedChat = chatRepository.save(chatMessageRequest.toEntity(chatPart, true));
      log.info("[sendMessage] 접속된 회원 메세지 저장 완료(Successfully saved message for connected user) ===>  savedChat: {}", savedChat);
    } else {
      savedChat = chatRepository.save(chatMessageRequest.toEntity(chatPart, false));
      log.info("[sendMessage] 미접속된 회원 메세지 저장 완료(Successfully saved message for disconnected user) ===>  savedChat: {}", savedChat);
      // 채팅방 업데이트
      sseEmitterService.sendMessageByClient(receiverId, chatRoomId, savedChat);
      NotificationDto notificationDto =
          NotificationDto.fromResponse(sender.getId(), sender.getUsername(), sender.getMainProfile(), savedChat.getMessage(),
                                       savedChat.getCreatedDate());
      sendNotification(receiverId, notificationDto);
    }
    ChatMessageResponse chatMessageResponse = ChatMessageResponse.fromDto(savedChat, sender, chatRoomId, LocalDateTime.now().toString());
    log.info("[sendMessage] 채팅 프로세스 완료 ===> chatMessageResponse: {}", chatMessageResponse);
    return chatMessageResponse;
  }

  private void sendNotification(Long receiverId, NotificationDto notificationDto) {
    notificationService.sendMessageTo(receiverId, notificationDto);
  }

  public PageResponse<ChatResponse> getChatMessagesWithPaging(final Long chatRoomId, final Long pageLastId, final int pageSize) {

    // 1. pageSize 만큼 가장 최근 채팅 메시지 조회
    Page<Chat> page = chatRepository.findChatsByChatRoomIdWithPaging(chatRoomId, pageLastId, pageSize);
    // 2. ChatResponse 변환 작업
    List<ChatResponse> chatResponseList = page.getContent().stream().map(ChatResponse::fromResponse).toList();
    log.debug("[getChatMessagesWithPaging] 채팅 메시지 페이징 조회(Chat messages retrieved with paging successfully) ===>  chatRoomId: {}", chatRoomId);
    return PageResponse.of(page.getSize(), page.isLast(), chatResponseList);
  }

  private Set<Long> isConnectedAllUser(final Long chatRoomId) {
    return redisUtil.getConnected(CHAT_ROOM_PREFIX + chatRoomId, Long.class);
  }

  private static String jwtParse(String token) {
    if (Strings.hasText(token) && token.startsWith(JwtProperties.TOKEN_PREFIX)) {
      return token.substring(7);
    }
    return null;
  }

}
