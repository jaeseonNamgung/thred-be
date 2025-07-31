package com.thred.datingapp.join.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.testFixture.UserFixture;
import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.user.UserDetail;
import com.thred.datingapp.common.entity.user.Picture;
import com.thred.datingapp.common.entity.user.Question;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.user.repository.UserDetailRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("test")
@DataJpaTest
@Import({P6SpyConfig.class, JpaConfig.class})
class UserDetailRepositoryTest {

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("회원 아이디로 세부 내용 조회")
    void findDetails() {
        User user = UserFixture.createCertificationUser1();
        em.persist(user);

        UserDetail userDetail = UserFixture.createDetails1(user);
        userDetailRepository.save(userDetail);

        UserDetail findUserDetail = userDetailRepository.findByUserId(user.getId()).get();

        assertThat(findUserDetail).isNotNull();
        assertThat(findUserDetail.getUser().getEmail()).isEqualTo("a");
    }

    @Test
    @DisplayName("모든 정보 한번에 조회하기")
    void findDetailsFetchUserInfo() {
        User user = UserFixture.createCertificationUser1();
        em.persist(user);
        UserDetail build = UserFixture.createDetails1(user);
        em.persist(build);
        Picture picture1 = UserFixture.createPicture1(user);
        Picture picture2 = UserFixture.createPicture2(user);
        Picture picture3 = UserFixture.createPicture3(user);
        em.persist(picture1);
        em.persist(picture2);
        em.persist(picture3);
        Question question = UserFixture.createQuestion1(user);
        em.persist(question);

        em.flush();
        em.clear();
        UserDetail findUserDetail = userDetailRepository.findByUserIdFetchUserInfo(user.getId()).get();
        assertThat(findUserDetail.getUser().getId()).isEqualTo(user.getId());
        assertThat(findUserDetail.getUser().getProfiles().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("deleteDetails - 해당 유저 아이디와 관련된 데이터는 모두 삭제 된다(1명일 경우).")
    void deleteAllByUserIdsWhenOne() {
        // given
        User user1 = UserFixture.createCertificationUser1();
        em.persist(user1);
        UserDetail userDetail = UserFixture.createDetails1(user1);
        em.persist(userDetail);
        em.flush();
        // when
        userDetailRepository.deleteAllByUserId(List.of(user1.getId()));
        em.flush();
        em.clear();
        // then
        Assertions.assertThat(em.find(UserDetail.class, userDetail.getId())).isNull();
    }

    @Test
    @DisplayName("deleteDetails - 해당 유저 아이디와 관련된 데이터는 모두 삭제 된다(2명 이상일 경우).")
    void  deleteAllByUserIdsWhenMore() {
        // given
        User user1 = UserFixture.createCertificationUser1();
        User user2 = UserFixture.createCertificationUser2();
        User user3 = UserFixture.createCertificationUser3();
        em.persist(user1);
        em.persist(user2);
        em.persist(user3);
        UserDetail userDetail1 = UserFixture.createDetails2(user1);
        UserDetail userDetail2 = UserFixture.createDetails2(user2);
        UserDetail userDetail3 = UserFixture.createDetails2(user3);
        em.persist(userDetail1);
        em.persist(userDetail2);
        em.persist(userDetail3);
        em.flush();
        // when
        userDetailRepository.deleteAllByUserId(List.of(user1.getId(), user2.getId()));
        em.flush();
        em.clear();
        // then
        Assertions.assertThat(em.find(UserDetail.class, userDetail1.getId())).isNull();
        Assertions.assertThat(em.find(UserDetail.class, userDetail2.getId())).isNull();
        Assertions.assertThat(em.find(UserDetail.class, userDetail3.getId())).isNotNull();
    }
}
