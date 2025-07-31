package com.thred.datingapp.chat.service;

import com.thred.datingapp.chat.dto.response.ChatRoomAllResponse;
import com.thred.datingapp.chat.dto.response.ChatRoomResponse;
import com.thred.datingapp.chat.repository.ChatPartRepository;
import com.thred.datingapp.chat.repository.ChatRoomRepository;
import com.thred.datingapp.chat.repository.queryDsl.ChatRepository;
import com.thred.datingapp.common.api.response.PageResponse;
import com.thred.datingapp.common.entity.chat.Chat;
import com.thred.datingapp.common.entity.chat.ChatPart;
import com.thred.datingapp.common.entity.chat.ChatRoom;
import com.thred.datingapp.common.entity.user.Picture;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.ChatErrorCode;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.common.utils.RedisUtils;
import com.thred.datingapp.main.service.CardService;
import com.thred.datingapp.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ChatPartRepository chatPartRepository;
    @Mock
    private ChatRepository chatRepository;
    @Mock
    private RedisUtils  redisUtil;
    @Mock
    private CardService cardService;

    @InjectMocks
    private ChatRoomService sut;


    @Test
    @DisplayName("[채팅방 생성] 채팅방이 생성될 경우 ChatRoomResponse를 반환")
    void createChatRoomTest() throws Exception {
        //given
        User userA = createMember("userA@test.com", "userA");
        User userB = createMember("userB@test.com", "userB");
        Picture.builder().s3Path("path").originalFileName("filename").user(userB).build();
        ChatRoom chatRoom = ChatRoom.createChatRoom();
        ReflectionTestUtils.setField(chatRoom, "id", 1L);
        given(userRepository.findById(any())).willReturn(Optional.of(userA));
        given(userRepository.findById(any())).willReturn(Optional.of(userB));

        given(chatRoomRepository.save(any())).willReturn(chatRoom);
        //when
        ChatRoomResponse expectedChatRoomResponse = sut.createChatRoom(1L, 2L);
        //then
        assertThat(expectedChatRoomResponse)
                .hasFieldOrPropertyWithValue("chatRoomId", 1L);

        then(userRepository).should(times(2)).findById(any());
        then(chatRoomRepository).should().save(any());
        then(chatPartRepository).should(times(2)).save(any());
        then(cardService).should().deleteCardOpen(anyLong(), anyLong());

    }

    @Test
    @DisplayName("[채팅방 생성] 회원이 존재하지 않을 경우 에러 발생")
    void createChatRoomTest2() throws Exception {
        //given
        given(userRepository.findById(any())).willReturn(Optional.empty());
        //when
        CustomException expectedException =
                (CustomException) catchException(() -> sut.createChatRoom(1L, 2L));
        //then
        assertThat(expectedException.getErrorCode().getHttpStatus()).isEqualTo(UserErrorCode.USER_NOT_FOUND.getHttpStatus());
        assertThat(expectedException.getMessage()).isEqualTo(UserErrorCode.USER_NOT_FOUND.getMessage());

        then(userRepository).should(times(1)).findById(any());

    }

    @Test
    @DisplayName("채팅방에 접속된 회원을 채팅방이 존재할 경우 Redis에 저장")
    void saveChatConnectionInfoTest() throws Exception {
        //given
        given(chatRoomRepository.existChatRoomByChatRoomId(any())).willReturn(true);

        //when
        sut.saveChatConnectionInfo(1L, 1L);
        //then
        then(redisUtil).should().saveConnected(any(), any());
        then(chatRoomRepository).should().existChatRoomByChatRoomId(any());
    }

    @Test
    @DisplayName("채팅방이 존재하지 않을 경우 상대측이 연결을 끊은 걸로 간주하고 NOT_FOUND_CHATROOM 에러를 보낸다.")
    void saveChatConnectionInfoTest2() throws Exception {
        //given
        given(chatRoomRepository.existChatRoomByChatRoomId(any())).willReturn(false);

        //when
        CustomException expectedException = (CustomException) catchException(() -> sut.saveChatConnectionInfo(1L, 1L));
        //then
        assertThat(expectedException.getErrorCode().getHttpStatus()).isEqualTo(ChatErrorCode.NOT_FOUND_CHATROOM.getHttpStatus());
        assertThat(expectedException.getMessage()).isEqualTo(ChatErrorCode.NOT_FOUND_CHATROOM.getMessage());
        then(chatRoomRepository).should().existChatRoomByChatRoomId(any());
    }

    @Test
    @DisplayName("현재 화면에 접속한 유저A 이외에 유저B가 접속되어 있으면 true를 리턴")
    void isConnectedTest() throws Exception {
        //given
        given(redisUtil.getConnected(any(), any())).willReturn(Set.of("userA@test.com", "userB@test.com"));

        //when
        boolean expectedBool = sut.isConnected(1L);
        //then
        assertThat(expectedBool).isTrue();
        then(redisUtil).should().getConnected(any(), any());
    }

    @Test
    @DisplayName("현재 화면에 접속한 유저A 이외에 유저B가 접속되어 있지 않다면 false를 리턴")
    void isConnectedTest2() throws Exception {
        //given
        given(redisUtil.getConnected(any(), any())).willReturn(Set.of("userA@test.com"));

        //when
        boolean expectedBool = sut.isConnected(1L);
        //then
        assertThat(expectedBool).isFalse();
        then(redisUtil).should().getConnected(any(), any());
    }

    @Test
    @DisplayName("[채팅방 전체 조회] 회원 이메일로 전체 채팅방을 조회 후 ChatRoomResponse를 반환 - 저장된 메시지가 없을 경우 메시지가 null인 Response를 반환")
    void getAllChatRoomsTest() throws Exception {
        //given

        // 회원 생성
        User userA = createMember("userA@test.com", "userA");
        User userB = createMember("userB@test.com", "userB");
        ReflectionTestUtils.setField(userA, "id", 1L);
        ReflectionTestUtils.setField(userB, "id", 2L);
        ReflectionTestUtils.setField(userA, "mainProfile", "mainProfile");
        ReflectionTestUtils.setField(userB, "mainProfile", "mainProfile");
        Picture.builder().s3Path("path").originalFileName("filename").user(userB).build();

        // 채팅방 및 파트 설정
        ChatRoom chatRoom = ChatRoom.createChatRoom();
        ReflectionTestUtils.setField(chatRoom, "id", 1L);

        // 채팅방에 대한 회원 설정
        ChatPart chatPartUserA = ChatPart.builder().user(userA).chatRoom(chatRoom).build();
        ReflectionTestUtils.setField(chatPartUserA, "id", 1L);
        // 각 채팅 파트너 채팅방에 대한 회원 설정
        ChatPart chatPartUserB = ChatPart.builder().user(userB).chatRoom(chatRoom).build();
        ReflectionTestUtils.setField(chatPartUserB, "id", 2L);
        // Chat 메시지 설정
        Chat chat1 = createChat("chat userA", chatPartUserA);
        Chat chat2 = createChat("chat userB", chatPartUserB);

        // Mock 동작 정의
        given(chatRoomRepository.findChatRoomsByUserIdWithPagination(any(), any(), any())).willReturn(new PageImpl<>(List.of(chatRoom)));
        given(chatRepository.findChatsByRoomId(any(), any())).willReturn(List.of(chat2));

        //when
        PageResponse<ChatRoomAllResponse> expectedResponse = sut.getAllChatRooms(1L, 0L, 5);

        //then
        assertThat(expectedResponse).isNotNull();
        assertThat(expectedResponse.pageSize()).isEqualTo(1);
        assertThat(expectedResponse.isLastPage()).isTrue();

        // 각 채팅방의 최근 메시지 및 파트너 정보 검증
        assertThat(expectedResponse.contents().get(0).message()).isEqualTo("chat userB");


        // 파트너 정보 검증 (기본 설정값을 사용한다고 가정)
        assertThat(expectedResponse.contents().get(0).receiverNickName()).isEqualTo("userB");
        assertThat(expectedResponse.contents().get(0).unReadCount()).isEqualTo(1);
        assertThat(expectedResponse.contents().get(0).receiverMainProfile()).isNotNull();

        // 호출 횟수 검증
        then(chatRoomRepository).should().findChatRoomsByUserIdWithPagination(any(), any(), any());
        then(chatRepository).should().findChatsByRoomId(any(), any());
    }


    @Test
    @DisplayName("채팅방 나갈 경우 채팅방 전체 삭제")
    void deleteChatRoomTest() throws Exception {
        //given
        given(chatRoomRepository.existChatRoomByChatRoomId(any())).willReturn(true);
        //when
        boolean expectedBool = sut.deleteChatRoom(1L);
        //then
        assertThat(expectedBool).isTrue();

        then(chatRoomRepository).should().existChatRoomByChatRoomId(any());
        then(chatRepository).should().deleteAllByChatRoomId(any());
        then(chatPartRepository).should().deleteAllByChatRoomId(any());
        then(chatRoomRepository).should().deleteByChatRoomId(any());

    }


    @Test
    @DisplayName("채팅방이 존재하지 않을 경우 NOT_FOUND_CHATROOM 에러를 보낸다.")
    void deleteChatRoomTest3() throws Exception {
        //given
        given(chatRoomRepository.existChatRoomByChatRoomId(any())).willReturn(false);
        //when
        CustomException expectedException = (CustomException) catchException(() -> sut.deleteChatRoom(1L));
        //then
        assertThat(expectedException.getErrorCode().getHttpStatus()).isEqualTo(ChatErrorCode.NOT_FOUND_CHATROOM.getHttpStatus());
        assertThat(expectedException.getMessage()).isEqualTo(ChatErrorCode.NOT_FOUND_CHATROOM.getMessage());

        then(chatRoomRepository).should().existChatRoomByChatRoomId(any());
    }

    @Test
    @DisplayName("회원 탈퇴 시 모든 ChatRoom, ChatPart, Chat 기록 삭제 후 true 를 반환")
    void deleteAllChatsForWithdrawnUserTestCase() {
        // given
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.existsById(any())).willReturn(true);
        given(chatPartRepository.findChatRoomIdAllByUserId(any())).willReturn(List.of(1L, 2L));
        // when
        Boolean expectedTrue = sut.deleteAllChatsForWithdrawnUser(1L);
        // then
        assertThat(expectedTrue).isTrue();
        then(userRepository).should().existsById(any());
        then(chatRepository).should().deleteAllByChatRoomIds(any());
        then(chatPartRepository).should().deleteAllByChatRoomIds(any());
        then(chatRoomRepository).should().deleteAllByChatRoomId(any());
    }


    @Test
    @DisplayName("회원 탈퇴 시 회원이 아무런 채팅 기록이 없을 경우 true 를 반환")
    void deleteAllChatsForWithdrawnUserTestCase2() {
        // given
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.existsById(any())).willReturn(true);
        given(chatPartRepository.findChatRoomIdAllByUserId(any())).willReturn(List.of());
        // when
        Boolean expectedTrue = sut.deleteAllChatsForWithdrawnUser(1L);
        // then
        assertThat(expectedTrue).isTrue();
        then(userRepository).should().existsById(any());
        then(chatPartRepository).should().findChatRoomIdAllByUserId(any());
    }

    @Test
    @DisplayName("회원이 존재하지 않을 경우 NOT_EXIST_USER 에러를 던진다")
    void deleteAllChatsForWithdrawnUserTestCase3() {
        // given
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.existsById(any())).willReturn(false);
        // when
        CustomException expectedException = (CustomException) catchException(() -> sut.deleteAllChatsForWithdrawnUser(1L));
        // then
        assertThat(expectedException.getErrorCode().getHttpStatus()).isEqualTo(UserErrorCode.USER_NOT_FOUND.getHttpStatus());
        assertThat(expectedException.getMessage()).isEqualTo(UserErrorCode.USER_NOT_FOUND.getMessage());
    }


    private static Chat createChat(String message, ChatPart chatParts) {
        return Chat.builder().message(message).readStatus(false).chatPart(chatParts).build();
    }

    private static User createMember(String email, String nickName) {
        return User.builder().email(email).username(nickName).build();
    }

    // 회원이 존재하지 않을 경우 예외 처리
    // 채팅방 -> 채팅 파트 -> 채팅 메시지 순으로 삭제
    @Test
    @DisplayName("채팅방이 존재하지 않을 경우 CustomException 예외 처리")
    void deleteChatRoomByAdmin_ifAllChatRoomNotExist_throwsCustomException() {
      // given
        Long targetId = 1L;
        Long userId = 1L;
        given(chatRoomRepository.findByChatRoomIdAndUserId(anyLong(), anyLong())).willReturn(Optional.empty());
      // when & then
        assertThatThrownBy(()->sut.deleteAllChatHistory(targetId, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ChatErrorCode.NOT_FOUND_CHATROOM);
        then(chatRoomRepository).should().findByChatRoomIdAndUserId(anyLong(), anyLong());
    }

    // 회원이 존재하지 않을 경우 예외 처리
    // 채팅방 -> 채팅 파트 -> 채팅 메시지 순으로 삭제
    @Test
    @DisplayName("채팅 메시지 -> 채팅 파트 -> 채팅방 순으로 삭제")
    void deleteChatRoomByAdmin_deleteAllAllChat() {
        // given
        Long targetId = 1L;
        Long userId = 1L;
        given(chatRoomRepository.findByChatRoomIdAndUserId(anyLong(), anyLong())).willReturn(Optional.of(ChatRoom.createChatRoom()));

        // when
        sut.deleteAllChatHistory(targetId, userId);

        //then
        then(chatRoomRepository).should().findByChatRoomIdAndUserId(anyLong(), anyLong());
        then(chatRepository).should().deleteAllByChatRoomId(anyLong());
        then(chatPartRepository).should().deleteAllByChatRoomId(anyLong());
        then(chatRoomRepository).should().deleteByChatRoomId(anyLong());
    }

}
