package com.thred.datingapp.main.repository;

import com.testFixture.UserFixture;
import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.card.Card;
import com.thred.datingapp.common.entity.card.CardOpen;
import com.thred.datingapp.common.entity.user.Block;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.UserDetail;
import com.thred.datingapp.common.entity.user.field.Gender;
import com.thred.datingapp.main.dto.response.CardOpenResponse;
import com.thred.datingapp.user.api.response.CardProfileResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import({JpaConfig.class, P6SpyConfig.class})
@ActiveProfiles("test")
@DataJpaTest
class CardRepositoryTest {

  @Autowired
  private CardRepository    cardRepository;
  @Autowired
  private CardOpenRepository    cardOpenRepository;
  @Autowired
  private TestEntityManager entityManager;



  /*
  * 자신의 카드 제외
  * 차단된 번호 제외
  * 이미 연결된 사람은 제외
  * 오픈한 카드 사람은 제외
  * 다른 성별만 보여지게
  * 당일 회원가입한 사람은 제외(회원가입을 한 경우 다음 날부터 다른 사람들의 오늘의 카드에 노출된다.)
  * */
  @DisplayName("전체 카드가 50장 이하 일 경우 30장만 보여준다.")
  @Test
  void findTodayRandomCardForViewerTest() {
    // given
    setUp(50);
    User viewer = entityManager.find(User.class, 1);
    entityManager.flush();
    entityManager.clear();
    // when

    List<CardProfileResponse> cards = cardRepository.findTodayRandomCardByViewerIdGenderCity(1L, Gender.MALE, "seoul");
    // then
    assertThat(cards.size()).isEqualTo(30);
    cards.forEach(card -> {
      assertThat(card.getUserId()).isNotEqualTo(viewer.getId());
      assertThat(card.getProvince()).isEqualTo("seoul");
    });

  }

  @DisplayName("전체 카드가 50장 이상 일 경우 70퍼센트만 보여준다.")
  @Test
  void findTodayRandomCardForViewerTest2() {
    // given
    setUp(1000);
    User viewer = entityManager.find(User.class, 1);
    entityManager.flush();
    entityManager.clear();
    // when
    List<CardProfileResponse> cards =
        cardRepository.findTodayRandomCardByViewerIdGenderCity(1L, Gender.MALE, "seoul");
    // then
    int expected = (int) Math.floor(1000 * 0.7);
    assertThat(cards.size())
        .isBetween(expected - 30, expected + 30);
    cards.forEach(card -> {
        assertThat(card.getUserId()).isNotEqualTo(viewer.getId());
        assertThat(card.getProvince()).isEqualTo("seoul");
    });

  }
  @Test
  @DisplayName("회원 탈퇴 카드, 오픈카드 삭제 테스트")
  void deleteById() {
    User viewer = UserFixture.createTestUser(1);
    entityManager.persist(viewer);
    Card viewerCard = UserFixture.createCard(viewer);
    entityManager.persist(viewerCard);

    User cardHolder1 = UserFixture.createTestUser(2);
    entityManager.persist(cardHolder1);
    Card card1 = UserFixture.createCard(cardHolder1);
    entityManager.persist(card1);

    User cardHolder2 = UserFixture.createTestUser(3);
    entityManager.persist(cardHolder2);
    Card card2 = UserFixture.createCard(cardHolder2);
    entityManager.persist(card2);

    CardOpen cardOpen1 = CardOpen.builder().opener(viewer).card(card1).build();
    entityManager.persist(cardOpen1);
    CardOpen cardOpen2 = CardOpen.builder().opener(viewer).card(card2).build();
    entityManager.persist(cardOpen2);
    CardOpen cardOpen3 = CardOpen.builder().opener(cardHolder1).card(viewerCard).build();
    entityManager.persist(cardOpen3);



    entityManager.flush();
    entityManager.clear();

    cardOpenRepository.deleteAllByCardIdOrOpenerId(viewerCard.getId(), viewer.getId());
    cardRepository.deleteById(viewerCard.getId());

    entityManager.flush();
    entityManager.clear();

    Page<CardOpenResponse> cardOpenResponseByOpenerId = cardOpenRepository.findCardOpenResponseByOpenerIdWithPaging(viewer.getId(), 0L, 20);
    Optional<CardOpen> cardOpenOptional = cardOpenRepository.findByCardId(viewerCard.getId());
    Optional<Card> cardOptional = cardRepository.findById(viewerCard.getId());
    assertThat(cardOpenResponseByOpenerId).isEmpty();
    assertThat(cardOpenOptional).isEmpty();
    assertThat(cardOptional).isEmpty();

  }


  private void setUp(int amount) {
    User viewer = UserFixture.createTestUser(1);
    entityManager.persist(viewer);
    UserDetail viewerDetail = UserFixture.createDetails1(viewer);
    entityManager.persist(viewerDetail);
    Card viewerCard = UserFixture.createCard(viewer);
    ReflectionTestUtils.setField(viewerCard, "createdDate", LocalDateTime.now().minusDays(2));
    entityManager.persist(viewerCard);

    for (int i = 2; i <= amount; i++) {
      User user = UserFixture.createTestUser(i);
      entityManager.persist(user);
      UserDetail userDetail = UserFixture.createDetails1(user);
      entityManager.persist(userDetail);
      Card card = UserFixture.createCard(user);
      ReflectionTestUtils.setField(card, "createdDate", LocalDateTime.now().minusDays(2));
      entityManager.persist(card);
    }

    for (int i = 2; i <= 5; i++) {
      User blockedUser = entityManager.find(User.class, i);
      Block block = Block.builder().blocker(viewer).blockedUser(blockedUser).build();
      entityManager.persist(block);
    }

    for (int i = 6; i <= 10; i++) {
      Card card = entityManager.find(Card.class, i);
      CardOpen cardOpen = CardOpen.builder().opener(viewer).card(card).build();
      entityManager.persist(cardOpen);
    }

    for (int i = 11; i <= 12; i++) {
      Card card = entityManager.find(Card.class, i);
      ReflectionTestUtils.setField(card, "createdDate", LocalDateTime.now());
    }

    entityManager.flush();
    entityManager.clear();
  }




}
