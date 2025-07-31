package com.thred.datingapp.user.repository;

import com.testFixture.UserFixture;
import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.user.*;
import com.thred.datingapp.user.api.request.BlockInfoRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import({P6SpyConfig.class, JpaConfig.class})
@ActiveProfiles("test")
@DataJpaTest
class UserRepositoryTest {

  @Autowired
  private UserRepository    userRepository;
  @Autowired
  private UserDetailRepository    userDetailRepository;
  @Autowired
  private PictureRepository    pictureRepository;
  @Autowired
  private RefreshTokenRepository    refreshTokenRepository;
  @Autowired
  private QuestionRepository    questionRepository;
  @Autowired
  private BlockRepository    blockRepository;
  @Autowired
  private TestEntityManager entityManager;

  @Test
  @DisplayName("이메일로 회원 조회 테스트")
  void givenEmail_whenUserExist_thenReturnUser() {
    // given
    User testUser = UserFixture.createTestUser(1);
    userRepository.save(testUser);
    // when
    User user = userRepository.findByEmail(testUser.getEmail()).get();
    // then
    assertThat(user).hasFieldOrPropertyWithValue("email", testUser.getEmail()).hasFieldOrPropertyWithValue("username", testUser.getUsername())
                    .hasFieldOrPropertyWithValue("id", testUser.getId());
  }

  @Test
  @DisplayName("[existsByUsername] 회원 이름으로 조회한 회원이 존재하면 true를 리턴한다.")
  void givenExistUsername_whenCheckDuplicateName_thenReturnsTrue() {
    // given
    User testUser = UserFixture.createTestUser(1);
    userRepository.save(testUser);
    // when
    boolean isExistUser = userRepository.existsByUsername(testUser.getUsername());
    // then
    assertThat(isExistUser).isTrue();
  }

  @Test
  @DisplayName("[existsByUsername] 회원 이름으로 조회한 회원이 존재하지 않으면 false를 리턴한다.")
  void givenNonExistingUsername_whenExistsByUsername_thenReturnsFalse() {
    // given
    User testUser = UserFixture.createTestUser(1);
    // when
    boolean isExistUser = userRepository.existsByUsername(testUser.getUsername());
    // then
    assertThat(isExistUser).isFalse();
  }

  @Test
  @DisplayName("[existsByCode] 회원 코드로 조회한 회원이 존재하면 true를 리턴한다.")
  void givenExistUsername_whenExistsByCode_thenReturnsTrue() {
    // given
    User testUser = UserFixture.createTestUser(1);
    userRepository.save(testUser);
    // when
    boolean isExistUser = userRepository.existsByCode(testUser.getCode());
    // then
    assertThat(isExistUser).isTrue();
  }

  @Test
  @DisplayName("[existsByCode] 회원 코드로 조회한 회원이 존재하지 않으면 false를 리턴한다.")
  void givenNonExistingUsername_whenExistsByCode_thenReturnsFalse() {
    // given
    User testUser = UserFixture.createTestUser(1);
    // when
    boolean isExistUser = userRepository.existsByUsername(testUser.getCode());
    // then
    assertThat(isExistUser).isFalse();
  }

  @Test
  @DisplayName("")
  void test() {
    // given
    User testUser = UserFixture.createTestUser(1);
    // when
    System.out.println("저장 전 id: " + testUser.getId());
    userRepository.save(testUser);
    // then
    System.out.println("저장 후 id: " + testUser.getId());
  }

  @Test
  @DisplayName("[findByEmailAndCertificationFalse] email 을 통해 certification false 인 회원 조회")
  void givenEmail_whenFindByEmailAndCertificationFalse_thenReturnsUser() {
    // given
    User user = UserFixture.createTestUser(1);
    ReflectionTestUtils.setField(user, "certification", false);
    userRepository.save(user);
    // when
    User expectedUser = userRepository.findByEmailAndCertificationFalse(user.getEmail()).get();
    // then
    assertThat(expectedUser.getCertification()).isFalse();
  }

  @Test
  void updateDirtyCheckingTest() {
    // given
    User user = UserFixture.createTestUser(1);
    userRepository.save(user);
    // when
    User findUser = userRepository.findById(user.getId()).get();
    findUser.changePhoneNumber("010-9717-5449");
    // then
    entityManager.flush();
    entityManager.clear();
    User finalUser = userRepository.findById(findUser.getId()).get();
    assertThat(finalUser.getPhoneNumber()).isEqualTo("010-9717-5449");

  }
  
  @Test
  void findBlockedUsersByPhoneNumberAndNameTest() {
    // given
    List<BlockInfoRequest> blockInfoRequests = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      User user = UserFixture.createTestUser(i);
      entityManager.persist(user);
      blockInfoRequests.add(new BlockInfoRequest(user.getUsername(), user.getPhoneNumber()));
    }
    // when
    List<User> blockedUsers = userRepository.findBlockedUsersByPhoneNumberAndName(blockInfoRequests);
    // then
    blockedUsers.forEach(u -> System.out.println(u.getUsername()));
    assertThat(blockedUsers.size()).isEqualTo(10);
  }

  @Test
  @DisplayName("회원 탈퇴 테스트")
  void withdrawUserTest() {
    setUp();
    User user = entityManager.find(User.class, 1L);
    UserDetail userDetail = entityManager.find(UserDetail.class, 1L);
    Block block = entityManager.find(Block.class, 1L);
    Picture picture = entityManager.find(Picture.class, 1L);
    RefreshToken refreshToken = entityManager.find(RefreshToken.class, 1L);
    Question question = entityManager.find(Question.class, 1L);
    assertThat(user).isNotNull();
    assertThat(userDetail).isNotNull();
    assertThat(block).isNotNull();
    assertThat(picture).isNotNull();
    assertThat(refreshToken).isNotNull();
    assertThat(question).isNotNull();
    entityManager.flush();
    entityManager.clear();

    userDetailRepository.deleteByUserId(user.getId());
    pictureRepository.deleteByUserId(user.getId());
    blockRepository.deleteByUserId(user.getId());
    questionRepository.deleteByUserId(user.getId());
    refreshTokenRepository.deleteByUserId(user.getId());
    userRepository.deleteByUserId(user.getId());
    entityManager.flush();
    entityManager.clear();

    user = entityManager.find(User.class, 1L);
    userDetail = entityManager.find(UserDetail.class, 1L);
    block = entityManager.find(Block.class, 1L);
    picture = entityManager.find(Picture.class, 1L);
    refreshToken = entityManager.find(RefreshToken.class, 1L);
    question = entityManager.find(Question.class, 1L);
    assertThat(user).isNull();
    assertThat(userDetail).isNull();
    assertThat(block).isNull();
    assertThat(picture).isNull();
    assertThat(refreshToken).isNull();
    assertThat(question).isNull();
  }

  private void setUp() {
    User user1 = UserFixture.createTestUser(1);
    User user2 = UserFixture.createTestUser(2);
    userRepository.save(user1);
    userRepository.save(user2);

    UserDetail userDetail = UserFixture.createDetails1(user1);
    entityManager.persist(userDetail);
    Block block = UserFixture.createBlock(user1, user2);
    entityManager.persist(block);
    Picture picture = UserFixture.createPicture1(user1);
    entityManager.persist(picture);
    RefreshToken refreshToken = UserFixture.createRefreshToken(user1);
    entityManager.persist(refreshToken);
    Question question = UserFixture.createQuestion1(user1);
    entityManager.persist(question);
    entityManager.flush();
    entityManager.clear();
  }

}
