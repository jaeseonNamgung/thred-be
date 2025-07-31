package com.thred.datingapp.main.service;

import com.testFixture.UserFixture;
import com.thred.datingapp.common.api.response.PageResponse;
import com.thred.datingapp.common.entity.card.Card;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.field.Gender;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.common.error.errorCode.MainErrorCode;
import com.thred.datingapp.common.utils.RedisUtils;
import com.thred.datingapp.main.repository.CardRepository;
import com.thred.datingapp.user.api.response.CardProfileResponse;
import com.thred.datingapp.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

  @InjectMocks
  private CardService    sut;
  @Mock
  private CardRepository cardRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private RedisUtils     redisUtils;

  @Test
  @DisplayName("redisUtils에서 캐시 조회 → 있으면 바로 반환")
  void getTodayRandomCard_CacheHit_ReturnsCachedPage() {
    // given
    User viewer = UserFixture.createTestUser(1);

    User profileUser = UserFixture.createTestUser(2);
    ReflectionTestUtils.setField(profileUser, "id", 2L);
    UserFixture.createDetails1(profileUser);
    Card card = UserFixture.createCard(profileUser);
    given(userRepository.findById(anyLong())).willReturn(Optional.of(viewer));
    given(redisUtils.getValue(anyString())).willReturn(List.of(card));
    // when
    PageResponse<CardProfileResponse> response = sut.getTodayRandomCard(1L, "seoul", 0, 5);
    // then
    assertThat(response.contents().get(0)).hasFieldOrPropertyWithValue("userId", profileUser.getId())
                                          .hasFieldOrPropertyWithValue("username", profileUser.getUsername())
                                          .hasFieldOrPropertyWithValue("city", profileUser.getAddress().getProvince())
                                          .hasFieldOrPropertyWithValue("height", profileUser.getUserDetail().getHeight())
                                          .hasFieldOrPropertyWithValue("age", profileUser.getAge());
    then(userRepository).should().findById(anyLong());
    then(redisUtils).should().getValue(anyString());
  }

  @Test
  @DisplayName("redisUtils에서 캐시 조회 → 없으면 DB에서 조회 후 반환")
  void getTodayRandomCard_CacheMiss_QueriesRepoAndCaches() {
    // given
    User viewer = UserFixture.createTestUser(1);

    User profileUser = UserFixture.createTestUser(2);
    ReflectionTestUtils.setField(profileUser, "id", 2L);
    UserFixture.createDetails1(profileUser);
    Card card = UserFixture.createCard(profileUser);
    CardProfileResponse cardProfileResponse =
        new CardProfileResponse(card.getId(), card.getProfileUser().getId(), card.getProfileUser().getUsername(),
                                card.getProfileUser().getMainProfile(), card.getProfileUser().getAge(), 175, "seoul", 90);
    given(userRepository.findById(anyLong())).willReturn(Optional.of(viewer));
    given(redisUtils.getValue(anyString())).willReturn(null);
    given(cardRepository.findTodayRandomCardByViewerIdGenderCity(anyLong(), any(Gender.class), anyString())).willReturn(List.of(cardProfileResponse));
    // when
    PageResponse<CardProfileResponse> response = sut.getTodayRandomCard(1L, "seoul", 0, 5);
    // then
    assertThat(response.contents().get(0)).hasFieldOrPropertyWithValue("userId", profileUser.getId())
                                          .hasFieldOrPropertyWithValue("username", profileUser.getUsername())
                                          .hasFieldOrPropertyWithValue("city", profileUser.getAddress().getProvince())
                                          .hasFieldOrPropertyWithValue("height", profileUser.getUserDetail().getHeight())
                                          .hasFieldOrPropertyWithValue("age", profileUser.getAge());
    then(userRepository).should().findById(anyLong());
    then(redisUtils).should().getValue(anyString());
    then(cardRepository).should().findTodayRandomCardByViewerIdGenderCity(anyLong(), any(Gender.class), anyString());
    then(redisUtils).should().saveWithTTL(anyString(), any(Object.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  @DisplayName("중복된 카드가 있으면 AlreadyMadeCard 에러 발생")
  void givenDuplicatedProfileUserId_whenCheckDuplicateCard_thenThrowsAlreadyMadeCardError() {
    // given
    given(cardRepository.existsByProfileUserId(anyLong())).willReturn(true);
    // when
    CustomException exception = assertThrows(CustomException.class, () -> sut.createCard(1L));
    // then
    assertThat(exception.getErrorCode()).isEqualTo(MainErrorCode.CARD_ALREADY_CREATED);
    then(cardRepository).should().existsByProfileUserId(anyLong());
  }

  @Test
  @DisplayName("회원이 존재하지 않으면 NotExistUser 에러 발생")
  void givenInvalidProfileUserId_whenCheckDuplicateCard_thenThrowsNotExistUserError() {
    // given
    given(cardRepository.existsByProfileUserId(anyLong())).willReturn(false);
    given(userRepository.findById(anyLong())).willReturn(Optional.empty());
    // when
    CustomException exception = assertThrows(CustomException.class, () -> sut.createCard(1L));
    // then
    assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    then(cardRepository).should().existsByProfileUserId(anyLong());
    then(userRepository).should().findById(anyLong());
  }

  @Test
  @DisplayName("중복된 카드가 없고 회원이 존재하면 카드를 저장")
  void givenValidProfileUserId_whenCheckDuplicateCard_thenReturnsVoid() {
    // given
    given(cardRepository.existsByProfileUserId(anyLong())).willReturn(false);
    given(userRepository.findById(anyLong())).willReturn(Optional.of(UserFixture.createTestUser(1)));
    // when
    sut.createCard(1L);
    // then
    then(cardRepository).should().existsByProfileUserId(anyLong());
    then(userRepository).should().findById(anyLong());
    then(cardRepository.save(any(Card.class)));
  }

}
