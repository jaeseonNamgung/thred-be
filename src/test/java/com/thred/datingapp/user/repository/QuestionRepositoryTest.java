package com.thred.datingapp.user.repository;

import com.testFixture.UserFixture;
import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.user.Question;
import com.thred.datingapp.common.entity.user.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import({JpaConfig.class, P6SpyConfig.class})
@ActiveProfiles("test")
@DataJpaTest
class QuestionRepositoryTest {

  @Autowired
  private QuestionRepository questionRepository;
  @Autowired
  private TestEntityManager  entityManager;


  @Test
  void deleteByIdTest() {
    // given
    Question question1 = UserFixture.createQuestion1(null);
    questionRepository.save(question1);
    // when
    questionRepository.deleteById(question1.getId());
    // then
    Optional<Question> question = questionRepository.findById(question1.getId());
    assertThat(question.isEmpty()).isTrue();
  }

  @Test
  void findByUserIdOrderByCreatedDateDescTest() {
    // given
    User user = UserFixture.createTestUser(1);
    entityManager.persist(user);
    Question question = UserFixture.createQuestion1(user);
    questionRepository.save(question);
    // when
    Optional<Question> optionalQuestion = questionRepository.findByUserIdOrderByCreatedDateDesc(user.getId());
    // then
    assertThat(optionalQuestion).isNotEmpty();
  }



}
