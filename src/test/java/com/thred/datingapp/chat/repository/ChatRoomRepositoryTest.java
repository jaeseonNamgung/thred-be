package com.thred.datingapp.chat.repository;

import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.chat.ChatPart;
import com.thred.datingapp.common.entity.chat.ChatRoom;
import com.thred.datingapp.common.entity.user.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Sql({"classpath:db/schema.sql","classpath:db/data.sql"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("local")
@Import({JpaConfig.class, P6SpyConfig.class})
@DataJpaTest
class ChatRoomRepositoryTest {

    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private ChatPartRepository chatPartRepository;
    @Autowired
    private EntityManager entityManager;


    @Test
    @DisplayName("채팅방이 존재할 경우 true를 리턴")
    void existChatRoomTest() throws Exception {
        //given
        //when
        boolean expectedBool = chatRoomRepository.existChatRoomByChatRoomId(1L);
        //then
        assertThat(expectedBool).isTrue();
    }

    @Test
    @DisplayName("채팅방이 존재하지 않을 경우 false를 리턴")
    void existChatRoomTest2() throws Exception {
        //given
        //when
        boolean expectedBool = chatRoomRepository.existChatRoomByChatRoomId(3L);
        //then
        assertThat(expectedBool).isFalse();

    }

    @Test
    @DisplayName("[QueryDsl] 회원 아이디로 모든 채팅방 조회 - 오늘의 카드 조회")
    void findChatPartByMemberEmailTest() throws Exception {
        //given
        //when
        Page<ChatRoom> expectedChatRoom =
                chatRoomRepository.findChatRoomsByUserIdWithPagination(1L, 1L, 5);
        //then
        assertThat(expectedChatRoom.isFirst()).isTrue();
        assertThat(expectedChatRoom.getSize()).isEqualTo(5);
        expectedChatRoom.getContent().forEach(chatRoom->{
            assertThat(chatRoom.getChatParts().get(0).getUser().getEmail()).isEqualTo("johndoe@example.com");
            assertThat(chatRoom.getChatParts().get(1).getUser().getEmail()).isEqualTo("janedoe@example.com");
        });

    }

    @Test
    @DisplayName("채팅방 전체 조회 - chatPart 페치 조인")
    void findChatRoomByIdTest() throws Exception {
        //given
        User userA = createMember("userA@test.com");
        User userB = createMember("userB@test.com");
        entityManager.persist(userA);
        entityManager.persist(userB);

        ChatRoom chatRoom = ChatRoom.createChatRoom();
        entityManager.persist(chatRoom);


        ChatPart chatPartUserA = ChatPart.builder().user(userA).chatRoom(chatRoom).build();
        ChatPart chatPartUserB = ChatPart.builder().user(userB).chatRoom(chatRoom).build();
        entityManager.persist(chatPartUserA);
        entityManager.persist(chatPartUserB);


        entityManager.flush();
        entityManager.clear();
        //when
        ChatRoom expectedChatRoom = chatRoomRepository.findChatRoomById(chatRoom.getId()).get();
        //then
        assertThat(expectedChatRoom.getChatParts().size()).isEqualTo(2);

    }

    @Test
    @DisplayName("채팅방 삭제 테스트")
    void deleteTest() throws Exception {
        //given
        //when & then
        ChatRoom savedChatRoom = chatRoomRepository.findChatRoomById(1L).get();
        chatPartRepository.deleteAll(savedChatRoom.getChatParts());
        entityManager.flush();
        entityManager.clear();
        chatRoomRepository.deleteById(savedChatRoom.getId());

        Optional<ChatRoom> expectedEmptyChatRoom = chatRoomRepository.findChatRoomById(1L);

        assertThat(expectedEmptyChatRoom).isEmpty();
    }


    private static User createMember(String email) {
        return User.builder().email(email).build();
    }


}
