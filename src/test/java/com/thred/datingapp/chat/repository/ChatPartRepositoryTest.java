package com.thred.datingapp.chat.repository;

import com.thred.datingapp.chat.repository.queryDsl.ChatRepository;
import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.chat.Chat;
import com.thred.datingapp.common.entity.chat.ChatPart;
import com.thred.datingapp.common.entity.chat.ChatRoom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql({"classpath:db/schema.sql", "classpath:db/data.sql"})
@ActiveProfiles("local")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, P6SpyConfig.class })
@DataJpaTest
class ChatPartRepositoryTest {

    @Autowired
    private ChatPartRepository chatPartRepository;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private ChatRepository chatRepository;

    @Test
    @DisplayName("채팅 아이디로 채팅 파트 조회")
    void findByRoomId() throws Exception {
        //given
        //when
        List<ChatPart> expectedChatParts = chatPartRepository.findByRoomId(1L);
        //then
        assertThat(expectedChatParts).isNotEmpty();
        assertThat(expectedChatParts.get(0).getUser().getEmail()).isEqualTo("johndoe@example.com");
        assertThat(expectedChatParts.get(1).getUser().getEmail()).isEqualTo("janedoe@example.com");
    }


    @Test
    @DisplayName("[QueryDsl]발신자 유저 이메일로 채팅방에 연결된 회원 정보와 채팅방, 채팅 메시지를 ChatPart로 조회")
    void findByChatRoomIdAndConnectorEmailTest() {
        // given
        // when
        ChatPart expectedOtherConnector = chatPartRepository.findByChatRoomIdAndOtherConnectorId(1L, 1L)
            .orElseThrow();
        // then
        assertThat(expectedOtherConnector.getChatRoom().getId()).isEqualTo(1L);
        assertThat(expectedOtherConnector.getChats().get(0).isReadStatus()).isEqualTo(false);
    }

    @Test
    @DisplayName("[JPA] 회원 탈퇴 채팅 기록 전체 삭제")
    void deleteAllTestCase() {
        // given
        // when
        List<Long> chatRoomIds = chatPartRepository.findChatRoomIdAllByUserId(1L);
        chatRepository.deleteAllByChatRoomIds(chatRoomIds);
        chatPartRepository.deleteAllByChatRoomIds(chatRoomIds);
        chatRoomRepository.deleteAllByChatRoomId(chatRoomIds);

        Page<Chat> expectedChatByChatRoomId1 = chatRepository.findChatsByChatRoomIdWithPaging(1L, 0L, 20);
        Page<Chat> expectedChatByChatRoomId2 = chatRepository.findChatsByChatRoomIdWithPaging(2L, 0L, 20);
        assertThat(expectedChatByChatRoomId1.getTotalElements()).isEqualTo(0);
        assertThat(expectedChatByChatRoomId1.getContent().size()).isEqualTo(0);
        assertThat(expectedChatByChatRoomId2.getTotalElements()).isEqualTo(0);
        assertThat(expectedChatByChatRoomId2.getContent().size()).isEqualTo(0);
        List<Long> expectedChatRoomIds = chatPartRepository.findChatRoomIdAllByUserId(1L);
        assertThat(expectedChatRoomIds).isEmpty();
        ChatRoom expectedChatRoomId1 = chatRoomRepository.findChatRoomById(1L).orElse(null);
        ChatRoom expectedChatRoomId2 = chatRoomRepository.findChatRoomById(2L).orElse(null);
        assertThat(expectedChatRoomId1).isNull();
        assertThat(expectedChatRoomId2).isNull();
    }


}
