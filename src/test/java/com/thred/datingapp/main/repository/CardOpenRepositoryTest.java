package com.thred.datingapp.main.repository;

import com.testFixture.UserFixture;
import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.card.Card;
import com.thred.datingapp.common.entity.card.CardOpen;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.UserDetail;
import com.thred.datingapp.main.dto.response.CardOpenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Import({JpaConfig.class, P6SpyConfig.class})
@DataJpaTest
class CardOpenRepositoryTest {
  
  @Autowired
  private CardOpenRepository cardOpenRepository;
  @Autowired
  private TestEntityManager  entityManager;
  
  @Test
  void findCardOpenResponseByOpenerIdTest() {
        // given
        openCardSetup(50);
        User viewer = entityManager.find(User.class, 1);
        entityManager.flush();
        entityManager.clear();
        // when
    Page<CardOpenResponse> cardOpenPage = cardOpenRepository.findCardOpenResponseByOpenerIdWithPaging(viewer.getId(), 0L, 20);
    // then
        assertThat(cardOpenPage.getContent().size()).isEqualTo(9);
  }

  @Test
  void deleteByOpenerIdAndCardIdTest() {
    // given
    openCardSetup(50);
    // when
    Card card = entityManager.find(Card.class, 2);
    cardOpenRepository.deleteByOpenerIdAndCardId(1L, card.getId());
    // then
    Optional<CardOpen> cardOpenOptional = cardOpenRepository.findByCardId(card.getId());
    List<CardOpen> cardOpens = cardOpenRepository.findAll();
    assertThat(cardOpenOptional.isEmpty()).isTrue();
    assertThat(cardOpens.size()).isEqualTo(8);
  }

    private void openCardSetup(int amount) {
      User viewer = UserFixture.createTestUser(1);
      entityManager.persist(viewer);
      UserDetail viewerDetail = UserFixture.createDetails1(viewer);
      entityManager.persist(viewerDetail);
      Card viewerCard = UserFixture.createCard(viewer);
      entityManager.persist(viewerCard);

      for (int i = 2; i <= amount; i++) {
        User user = UserFixture.createTestUser(i);
        entityManager.persist(user);
        UserDetail userDetail = UserFixture.createDetails1(user);
        entityManager.persist(userDetail);
        Card card = Card.builder().profileUser(user).build();
        entityManager.persist(card);
      }

      for (int i = 2; i <= 10; i++) {
        Card card = entityManager.find(Card.class, i);
        CardOpen cardOpen = CardOpen.builder().opener(viewer).card(card).build();
        entityManager.persist(cardOpen);
      }

      entityManager.flush();
      entityManager.clear();
    }
  
}
