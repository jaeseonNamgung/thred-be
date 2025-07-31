package com.thred.datingapp.chat.service;

import com.thred.datingapp.chat.dto.ChatMessageResponse;
import com.thred.datingapp.chat.dto.request.ChatMessageRequest;
import com.thred.datingapp.chat.repository.ChatPartRepository;
import com.thred.datingapp.chat.repository.queryDsl.ChatRepository;
import com.thred.datingapp.common.entity.chat.Chat;
import com.thred.datingapp.common.entity.chat.ChatPart;
import com.thred.datingapp.common.entity.chat.ChatRoom;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.BaseErrorCode;
import com.thred.datingapp.common.service.NotificationService;
import com.thred.datingapp.common.utils.JwtUtils;
import com.thred.datingapp.common.utils.RedisUtils;
import com.thred.datingapp.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @Mock
  private ChatRepository    chatRepository;
  @Mock
  private RedisUtils        redisUtil;
  @Mock
  private SseEmitterService sseEmitterService;
  @Mock
  private UserService       userService;
  @Mock
  private JwtUtils          jwtUtils;

  @Mock
  private ChatPartRepository    chatPartRepository;
  @Mock
  private NotificationService   notificationService;
  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @InjectMocks
  private ChatService sut;


  @ValueSource(strings = {"connect", "disconnect"})
  @ParameterizedTest
  @DisplayName("접속된 다른 유저가 있으면 접속 메시지 전송")
  void notifyUserChatRoomEventTestCase_3(String eventType) {
    // given
    User user = createUser("testUser@test.com");
    ReflectionTestUtils.setField(user, "id", 1L);
    given(userService.getUserById(anyLong())).willReturn(user);
    // when
    sut.notifyUserChatRoomEvent(1L, 1L, eventType);
    // then
    then(userService).should().getUserById(anyLong());
    then(messagingTemplate).should().convertAndSend(anyString(), any(ChatMessageResponse.class));
  }



  @Test
  @DisplayName("ChatPart가 존재하지 않으면 예외 처리")
  void updateReadStatusTestCase_1() {
    // given
    given(chatPartRepository.findByChatRoomIdAndOtherConnectorId(anyLong(), anyLong())).willReturn(Optional.empty());
    // when
    assertThatThrownBy(()-> sut.updateReadStatus(1L, 1L))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", BaseErrorCode.INTERNAL_SERVER_ERROR);
    // then
    then(chatPartRepository).should().findByChatRoomIdAndOtherConnectorId(anyLong(), anyLong());
  }

  @Test
  @DisplayName("ChatPart -> Chat 이 empty가 아니라면 ReadStatus 상태 업데이트")
  void updateReadStatusTestCase_2() {
    // given
    User user = createUser("test@test.com");
    ChatPart chatPart = createChatPart(ChatRoom.createChatRoom(), user);
    ReflectionTestUtils.setField(chatPart, "id", 1L);
    createChat(3, chatPart);
    given(chatPartRepository.findByChatRoomIdAndOtherConnectorId(anyLong(), anyLong())).willReturn(Optional.of(chatPart));
    // when
    sut.updateReadStatus(1L, 1L);
    // then
    then(chatPartRepository).should().findByChatRoomIdAndOtherConnectorId(anyLong(), anyLong());
    then(chatRepository).should().updateReadStatusByChatPartId(anyLong());
  }

  @Test
  @DisplayName("접속 중이 회원일 경우 모든 메세지를 읽음 처리")
  void updateReadStatus() throws Exception {
    //given
    Long chatId = 1L;
    Long userId = 1L;
    ChatRoom chatRoom = ChatRoom.createChatRoom();
    User userB = createUser("userB@email.com");
    ChatPart chatPart = createChatPart(chatRoom, userB);

    for (int i = 0; i < 5; i++) {
      createChat(i, chatPart);
    }
    given(chatPartRepository.findByChatRoomIdAndOtherConnectorId(any(), any())).willReturn(Optional.of(chatPart));
    //when
    sut.updateReadStatus(chatId, userId);
    //then
    then(chatPartRepository).should().findByChatRoomIdAndOtherConnectorId(any(), any());
    then(chatRepository).should().updateReadStatusByChatPartId(any());
  }

  @Test
  @DisplayName("채팅 메세지 전송 테스트 - 상대방이 접속된 상태라면 메세지를 읽음 처리 후 저장")
  void sendMessageTest() throws Exception {
    //given
    ChatRoom chatRoom = ChatRoom.createChatRoom();
    User userA = createUser("userA@email.com");
    ChatPart chatPart = createChatPart(chatRoom, userA);
    Chat chat = createChat(1, chatPart);
    ChatMessageRequest chatMessageRequest = createChatMessageRequest();
    ReflectionTestUtils.setField(chat, "createdDate", LocalDateTime.now());

    given(jwtUtils.getUserId(any())).willReturn(1L);
    given(userService.getUserById(any())).willReturn(userA);
    given(redisUtil.getConnected(any(), any())).willReturn(Set.of(1L, 2L));
    given(chatPartRepository.findByChatRoomIdAndUserId(any(), any())).willReturn(Optional.of(chatPart));
    given(chatRepository.save(any())).willReturn(chat);

    //when
    ChatMessageResponse chatMessageResponse = sut.sendMessage(1L, "Bearer accessToken", chatMessageRequest);
    //then
    assertThat(chatMessageResponse).isNotNull();

    then(jwtUtils).should().getUserId(any());
    then(userService).should().getUserById(any());
    then(redisUtil).should().getConnected(any(), any());
    then(chatPartRepository).should().findByChatRoomIdAndUserId(any(), any());
    then(chatRepository).should().save(any());
  }

  @Test
  @DisplayName("채팅 메세지 전송 테스트 - 미 접속된 회원이라면 메시지 알림을 보낸 후 전송")
  void sendMessageTest2() throws Exception {
    //given
    ChatRoom chatRoom = ChatRoom.createChatRoom();
    User userA = createUser("userA@email.com");
    ChatPart chatPart = createChatPart(chatRoom, userA);
    Chat chat = createChat(1, chatPart);
    ChatMessageRequest chatMessageRequest = createChatMessageRequest();
    ReflectionTestUtils.setField(chat, "createdDate", LocalDateTime.now());

    given(jwtUtils.getUserId(any())).willReturn(1L);
    given(userService.getUserById(any())).willReturn(userA);
    given(redisUtil.getConnected(any(), any())).willReturn(Set.of(1L));
    given(chatPartRepository.findByChatRoomIdAndUserId(any(), any())).willReturn(Optional.of(chatPart));
    given(chatRepository.save(any())).willReturn(chat);

    //when
    ChatMessageResponse chatMessageResponse = sut.sendMessage(1L, "Bearer accessToken", chatMessageRequest);
    //then
    assertThat(chatMessageResponse).isNotNull();

    then(jwtUtils).should().getUserId(any());
    then(userService).should().getUserById(any());
    then(redisUtil).should().getConnected(any(), any());
    then(chatPartRepository).should().findByChatRoomIdAndUserId(any(), any());
    then(chatRepository).should().save(any());
    then(sseEmitterService).should().sendMessageByClient(any(), any(), any());
    then(notificationService).should().sendMessageTo(any(), any());

  }

  @Test
  @DisplayName("채팅 메세지 전송 테스트 - 채팅방을 나갈경우 로직 테스트")
  void sendMessageTest3() throws Exception {
    //given
    ChatRoom chatRoom = ChatRoom.createChatRoom();
    User userA = createUser("userA@email.com");
    ChatPart chatPart = createChatPart(chatRoom, userA);
    Chat chat = createChat(1, chatPart);
    ChatMessageRequest chatMessageRequest = ChatMessageRequest.of(2L, "", true);
    ReflectionTestUtils.setField(chat, "createdDate", LocalDateTime.now());

    given(jwtUtils.getUserId(any())).willReturn(1L);
    given(userService.getUserById(any())).willReturn(userA);

    //when
    ChatMessageResponse chatMessageResponse = sut.sendMessage(1L, "Bearer accessToken", chatMessageRequest);
    //then
    assertThat(chatMessageResponse).hasFieldOrPropertyWithValue("chatId", 0L)
                                   .hasFieldOrPropertyWithValue("message", userA.getUsername() + "님이 채팅방을 나갔습니다.")
                                   .hasFieldOrPropertyWithValue("isPartnerLeftChatRoom", true);

    then(jwtUtils).should().getUserId(any());
    then(userService).should().getUserById(any());

  }

  private static ChatMessageRequest createChatMessageRequest() {
    return ChatMessageRequest.of(2L, "message", false);
  }

  private static Chat createChat(int i, ChatPart chatPart) {
    return Chat.builder().message("test message" + i).readStatus(false).chatPart(chatPart).build();
  }

  private static ChatPart createChatPart(ChatRoom chatRoom, User userB) {
    return ChatPart.builder().chatRoom(chatRoom).user(userB).build();
  }

  private static User createUser(String email) {
    return User.builder().email(email).build();
  }

}
