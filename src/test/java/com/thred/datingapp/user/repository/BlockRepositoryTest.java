package com.thred.datingapp.user.repository;

import com.testFixture.UserFixture;
import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.user.Block;
import com.thred.datingapp.common.entity.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import({JpaConfig.class, P6SpyConfig.class})
@ActiveProfiles("test")
@DataJpaTest
class BlockRepositoryTest {

  @Autowired
  private BlockRepository blockRepository;
  @Autowired
  private UserRepository userRepository;


  @Test
  void deleteByBlockerIdsTest() {
    // given
    User user1 = UserFixture.createTestUser(1);
    User user2 = UserFixture.createTestUser(2);
    userRepository.save(user1);
    userRepository.save(user2);
    Block testUserName1 = Block.builder().blockedUser(user1).blocker(user2).build();
    Block testUserName2 = Block.builder().blockedUser(user2).blocker(user1).build();
    blockRepository.save(testUserName1);
    blockRepository.save(testUserName2);

    List<Long> userIds = userRepository.findAll().stream().map(User::getId).toList();
    // when
    blockRepository.deleteByUserIds(userIds);
    // then
    List<Block> blocks = blockRepository.findAll();

    assertThat(blocks).isEmpty();
  }

}
