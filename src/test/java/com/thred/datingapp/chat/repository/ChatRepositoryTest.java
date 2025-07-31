package com.thred.datingapp.chat.repository;

import com.thred.datingapp.chat.repository.queryDsl.ChatRepository;
import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.chat.Chat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@Import({JpaConfig.class, P6SpyConfig.class})
@ActiveProfiles("local")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class ChatRepositoryTest {

  @Autowired
  private ChatRepository chatRepository;

  @Test
  @DisplayName("[QueryDsl] 채팅 페이징 조회- 가장 마직막으로 생성된 채팅 id 부터 페이징 처리")
  void findByChatRoomIdWithPagingTestCase() {
    // given
    // when
    Page<Chat> expectedPage = chatRepository.findChatsByChatRoomIdWithPaging(2L, 0L, 5);
    // then
    assertThat(expectedPage.getSize()).isEqualTo(5);
    assertThat(expectedPage.getContent().get(0).getId()).isEqualTo(154L);
    assertThat(expectedPage.getContent().get(1).getId()).isEqualTo(758L);
    assertThat(expectedPage.getContent().get(2).getId()).isEqualTo(198L);
    assertThat(expectedPage.getContent().get(3).getId()).isEqualTo(646L);
    assertThat(expectedPage.getContent().get(4).getId()).isEqualTo(26L);

  }

  @Test
  @DisplayName("[QueryDsl] 채팅 페이징 조회- 특정 채팅 id 부터 페이징 처리")
  void findByChatRoomIdWithPagingTestCase2() {
    // given
    // when
    Page<Chat> expectedPage = chatRepository.findChatsByChatRoomIdWithPaging(1L, 4L, 5);
    // then
    assertThat(expectedPage.getSize()).isEqualTo(5);
    assertThat(expectedPage.isLast()).isEqualTo(true);
    assertThat(expectedPage.getContent().get(0).getId()).isEqualTo(3L);
    assertThat(expectedPage.getContent().get(1).getId()).isEqualTo(2L);
    assertThat(expectedPage.getContent().get(2).getId()).isEqualTo(1L);

  }
  
  @Test
  @DisplayName("[JPA] 채팅방에 있는 모든 채팅 메시지 삭제")
  void deleteAllByChatRoomIdTestCase() {
    // given
    Page<Chat> beforeDeleteChat = chatRepository.findChatsByChatRoomIdWithPaging(1L, 0L, 5);
    assertThat(beforeDeleteChat.getContent()).isNotEmpty();
    // when
      chatRepository.deleteAllByChatRoomId(1L);
    // then
    Page<Chat> afterDeleteChat = chatRepository.findChatsByChatRoomIdWithPaging(1L, 0L, 5);
    assertThat(afterDeleteChat.getContent()).isEmpty();
  }

  @Test
  @DisplayName("[QueryDsl] 읽지 안은 메시지 수 조회 테스트")
  void countUnReadChatMessageByReceiverIdTestCase() {
    // given

    // when
    Long expectedCount = chatRepository.countUnReadChatMessageByReceiverId(1L);
    // then
    assertThat(expectedCount).isEqualTo(10L);
  }

}
