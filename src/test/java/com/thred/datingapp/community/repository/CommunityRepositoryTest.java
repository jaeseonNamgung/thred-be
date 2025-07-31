package com.thred.datingapp.community.repository;

import com.thred.datingapp.common.config.JpaConfig;
import com.thred.datingapp.common.config.P6SpyConfig;
import com.thred.datingapp.common.entity.community.Community;
import com.thred.datingapp.common.entity.community.type.CommunityType;
import com.thred.datingapp.community.dto.response.CommunityAllResponse;
import com.thred.datingapp.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import({JpaConfig.class, P6SpyConfig.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class CommunityRepositoryTest {

  @Autowired
  private CommunityRepository communityRepository;
  @Autowired
  private UserRepository      userRepository;
  @Autowired
  private EntityManager       entityManager;

  @Test
  @DisplayName("[QueryDsl] 실시간 기준으로 커뮤니티 전체 조회")
  void findCommunityAllTest() {
    // given

    // when
    Page<CommunityAllResponse> expectedPage =
        communityRepository.findCommunitiesByCommunityTypeAndPageLastIdWithPaging(CommunityType.REAL_TIME, 0L, 5);
    List<CommunityAllResponse> expectedResponse = expectedPage.getContent();
    // then
    assertThat(expectedResponse.size()).isEqualTo(4);

    assertThat(expectedResponse.get(0).getCommunityId()).isEqualTo(4L);
    assertThat(expectedResponse.get(0).getTitle()).isEqualTo("Community Post 4");
    assertThat(expectedResponse.get(1).getCommunityId()).isEqualTo(3L);
    assertThat(expectedResponse.get(1).getTitle()).isEqualTo("Community Post 3");
    assertThat(expectedResponse.get(2).getCommunityId()).isEqualTo(2L);
    assertThat(expectedResponse.get(2).getTitle()).isEqualTo("Community Post 2");
    assertThat(expectedResponse.get(3).getCommunityId()).isEqualTo(1L);
    assertThat(expectedResponse.get(3).getTitle()).isEqualTo("Community Post 1");

  }

  @Test
  @DisplayName("[QueryDsl] 핫 기준으로 커뮤니티 전체 조회")
  void findCommunityAllTest2() {
    // given

    // when
    Page<CommunityAllResponse> expectedPage = communityRepository.findCommunitiesByCommunityTypeAndPageLastIdWithPaging(CommunityType.HOT, 0L, 5);
    List<CommunityAllResponse> expectedResponse = expectedPage.getContent();
    // then
    assertThat(expectedResponse.get(0).getCommunityId()).isEqualTo(1L);
    assertThat(expectedResponse.get(0).getTitle()).isEqualTo("Community Post 1");
  }

  @Test
  @DisplayName("[QueryDsl] 실시간 기준으로 6 이후에 데이터 페이징 조회")
  void findCommunityAllTest3() {

    for (int i = 5; i <= 10; i++) {
      Community community = Community.builder()
                                     .title("Community Post " + i)
                                     .content("content test paging " + i)
                                     .isPublicProfile(true)
                                     .user(userRepository.findById(1L).get())
                                     .build();
      ReflectionTestUtils.setField(community, "id", (long) i);
      ReflectionTestUtils.setField(community, "createdDate", LocalDateTime.now());
      communityRepository.save(community);
    }

    Page<CommunityAllResponse> expectedPage =
        communityRepository.findCommunitiesByCommunityTypeAndPageLastIdWithPaging(CommunityType.REAL_TIME, 6L, 5);

    List<CommunityAllResponse> expectedResponse = expectedPage.getContent();
    assertThat(expectedResponse.size()).isEqualTo(5);
    assertThat(expectedResponse.get(0).getCommunityId()).isEqualTo(5L);
    assertThat(expectedResponse.get(0).getTitle()).isEqualTo("Community Post 5");
    assertThat(expectedResponse.get(1).getCommunityId()).isEqualTo(4L);
    assertThat(expectedResponse.get(1).getTitle()).isEqualTo("Community Post 4");
    assertThat(expectedResponse.get(2).getCommunityId()).isEqualTo(3L);
    assertThat(expectedResponse.get(2).getTitle()).isEqualTo("Community Post 3");
    assertThat(expectedResponse.get(3).getCommunityId()).isEqualTo(2L);
    assertThat(expectedResponse.get(3).getTitle()).isEqualTo("Community Post 2");
    assertThat(expectedResponse.get(4).getCommunityId()).isEqualTo(1L);
    assertThat(expectedResponse.get(4).getTitle()).isEqualTo("Community Post 1");

  }

  @Test
  @DisplayName("[QueryDsl] 실시간 기준으로 커뮤니티 아이디를 0으로 전달해서 처음 페이지를 조회")
  void findCommunityAllTest4() {

    for (int i = 5; i <= 10; i++) {
      Community community = Community.builder()
                                     .title("Community Post " + i)
                                     .content("content test paging " + i)
                                     .isPublicProfile(true)
                                     .user(userRepository.findById(1L).get())
                                     .build();
      ReflectionTestUtils.setField(community, "id", (long) i);
      ReflectionTestUtils.setField(community, "createdDate", LocalDateTime.now());
      communityRepository.save(community);
    }

    Page<CommunityAllResponse> expectedPage =
        communityRepository.findCommunitiesByCommunityTypeAndPageLastIdWithPaging(CommunityType.REAL_TIME, 0L, 5);

    List<CommunityAllResponse> expectedResponse = expectedPage.getContent();
    assertThat(expectedResponse.size()).isEqualTo(5);
    assertThat(expectedResponse.get(0).getCommunityId()).isEqualTo(10L);
    assertThat(expectedResponse.get(0).getTitle()).isEqualTo("Community Post 10");
    assertThat(expectedResponse.get(1).getCommunityId()).isEqualTo(9L);
    assertThat(expectedResponse.get(1).getTitle()).isEqualTo("Community Post 9");
    assertThat(expectedResponse.get(2).getCommunityId()).isEqualTo(8L);
    assertThat(expectedResponse.get(2).getTitle()).isEqualTo("Community Post 8");
    assertThat(expectedResponse.get(3).getCommunityId()).isEqualTo(7L);
    assertThat(expectedResponse.get(3).getTitle()).isEqualTo("Community Post 7");
    assertThat(expectedResponse.get(4).getCommunityId()).isEqualTo(6L);
    assertThat(expectedResponse.get(4).getTitle()).isEqualTo("Community Post 6");

  }

  @Test
  @DisplayName("[QueryDsl] 실시간 기준으로 커뮤니티 아이디를 null 로 전달해서 처음 페이지를 조회")
  void findCommunityAllTest5() {

    for (int i = 5; i <= 10; i++) {
      Community community = Community.builder()
                                     .title("Community Post " + i)
                                     .content("content test paging " + i)
                                     .isPublicProfile(true)
                                     .user(userRepository.findById(1L).get())
                                     .build();
      ReflectionTestUtils.setField(community, "id", (long) i);
      ReflectionTestUtils.setField(community, "createdDate", LocalDateTime.now());
      communityRepository.save(community);
    }

    Page<CommunityAllResponse> expectedPage =
        communityRepository.findCommunitiesByCommunityTypeAndPageLastIdWithPaging(CommunityType.REAL_TIME, null, 5);

    List<CommunityAllResponse> expectedResponse = expectedPage.getContent();
    assertThat(expectedResponse.size()).isEqualTo(5);
    assertThat(expectedResponse.get(0).getCommunityId()).isEqualTo(10L);
    assertThat(expectedResponse.get(0).getTitle()).isEqualTo("Community Post 10");
    assertThat(expectedResponse.get(1).getCommunityId()).isEqualTo(9L);
    assertThat(expectedResponse.get(1).getTitle()).isEqualTo("Community Post 9");
    assertThat(expectedResponse.get(2).getCommunityId()).isEqualTo(8L);
    assertThat(expectedResponse.get(2).getTitle()).isEqualTo("Community Post 8");
    assertThat(expectedResponse.get(3).getCommunityId()).isEqualTo(7L);
    assertThat(expectedResponse.get(3).getTitle()).isEqualTo("Community Post 7");
    assertThat(expectedResponse.get(4).getCommunityId()).isEqualTo(6L);
    assertThat(expectedResponse.get(4).getTitle()).isEqualTo("Community Post 6");

  }

  @Test
  @DisplayName("[QueryDsl] 커뮤니티 단건 조회")
  void findCommunityByIdTest() {
    // given

    // when
    Community expectedCommunity = communityRepository.findByCommunityId(1L).orElseThrow(null);
    // then
    // 게시글 테스트
    assertThat(expectedCommunity).isNotNull();
    assertThat(expectedCommunity.getTitle()).isEqualTo("Community Post 1");
    assertThat(expectedCommunity.getContent()).isEqualTo("This is the first community post.");
    assertThat(expectedCommunity.getIsPublicProfile()).isTrue();

    // 회원 테스트
    assertThat(expectedCommunity.getUser()).isNotNull();
    assertThat(expectedCommunity.getUser().getUsername()).isEqualTo("JohnDoe");
    assertThat(expectedCommunity.getUser().getEmail()).isEqualTo("johndoe@example.com");

    // 게시글 이미지 테스트
    assertThat(expectedCommunity.getCommunityImages()).isNotNull();
    assertThat(expectedCommunity.getCommunityImages().size()).isEqualTo(2);
    assertThat(expectedCommunity.getCommunityImages().get(0).getOriginalFileName()).isEqualTo("/images/community1_img1.jpg");
    assertThat(expectedCommunity.getCommunityImages().get(1).getOriginalFileName()).isEqualTo("/images/community1_img2.jpg");

  }

  @Test
  @DisplayName("[회원탈퇴] 회원과 연관된 커뮤니티의 회원 정보를 null로 설정한다.")
  void detachUserFromCommunities() {
    // given
    String jpql = "SELECT c FROM Community c WHERE c.id = 1L";
    Community community = entityManager.createQuery(jpql, Community.class).getSingleResult();
    entityManager.flush();
    entityManager.clear();
    // when
    communityRepository.detachUserFromCommunities(1L);
    // then
    Community expectedCommunity = entityManager.createQuery(jpql, Community.class).getSingleResult();

    assertThat(expectedCommunity.getId()).isEqualTo(community.getId());
    assertThat(expectedCommunity.getUser()).isNull();
    assertThat(community.getUser().getId()).isEqualTo(1L);
  }

}
