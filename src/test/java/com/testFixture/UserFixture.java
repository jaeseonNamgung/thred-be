package com.testFixture;

import static com.thred.datingapp.common.entity.user.field.Belief.BUDDHISM;
import static com.thred.datingapp.common.entity.user.field.Belief.ELSE;
import static com.thred.datingapp.common.entity.user.field.Job.PART_TIME;
import static com.thred.datingapp.common.entity.user.field.Job.STUDENT;
import static com.thred.datingapp.common.entity.user.field.Mbti.ENFJ;
import static com.thred.datingapp.common.entity.user.field.Mbti.INFJ;
import static com.thred.datingapp.common.entity.user.field.Mbti.ISFJ;
import static com.thred.datingapp.common.entity.user.field.OppositeFriends.A_FEW;
import static com.thred.datingapp.common.entity.user.field.OppositeFriends.NO;
import static com.thred.datingapp.common.entity.user.field.Smoke.ELECTRONIC_CIGARETTE;
import static com.thred.datingapp.common.entity.user.field.Smoke.NO_SMOKE;
import static com.thred.datingapp.common.entity.user.field.Smoke.SMOKER;

import com.thred.datingapp.common.entity.card.Card;
import com.thred.datingapp.common.entity.user.*;
import com.thred.datingapp.common.entity.user.field.*;
import com.thred.datingapp.user.api.request.JoinDetailsRequest;
import com.thred.datingapp.user.api.request.JoinUserRequest;
import com.thred.datingapp.user.api.request.OAuthLoginRequest;
import com.thred.datingapp.user.dto.KakaoDto;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UserFixture {
  public static User createAdmin() {
    return User.builder()
               .email("admin")
               .mainProfile("a")
               .partnerGender(PartnerGender.OTHER)
               .role(Role.ADMIN)
               .introduce("a")
               .username("admin")
               .build();
  }

  public static User createCertificationUser1() {

    return User.builder()
               .email("a")
               .mainProfile("a")
               .partnerGender(PartnerGender.OTHER)
               .role(Role.USER)
               .introduce("a")
               .username("a")
               .code("123456")
               .certification(true)
               .address(Address.of("서울시", "강남구"))
               .build();
  }

  public static User createCertificationUser2() {
    return User.builder()
               .email("b")
               .mainProfile("b")
               .partnerGender(PartnerGender.OTHER)
               .role(Role.USER)
               .introduce("b")
               .username("b")
               .certification(true)
               .build();
  }

  public static User createCertificationUser3() {
    return User.builder()
               .email("c")
               .mainProfile("c")
               .partnerGender(PartnerGender.OTHER)
               .role(Role.USER)
               .introduce("c")
               .username("c")
               .certification(true)
               .build();
  }

  public static User createCertificationUser4() {
    return User.builder()
               .email("d")
               .mainProfile("d")
               .partnerGender(PartnerGender.OTHER)
               .role(Role.USER)
               .introduce("d")
               .username("d")
               .certification(true)
               .build();
  }

  public static User createQuitUser(LocalDateTime quitTime) {
    return User.builder()
               .email("a")
               .mainProfile("a")
               .partnerGender(PartnerGender.OTHER)
               .role(Role.USER)
               .introduce("a")
               .username("a")
               .code("123456")
               .certification(true)
               .address(Address.of("서울시", "강남구"))
               .build();
  }

  public static UserDetail createDetails1(User user) {
    return UserDetail.builder()
                     .user(user)
                     .mbti(ENFJ)
                     .drink(Drink.ENJOY)
                     .height(100)
                     .smoke(ELECTRONIC_CIGARETTE)
                     .temperature(10)
                     .oppositeFriends(NO)
                     .belief(ELSE)
                     .job(STUDENT)
                     .build();
  }

  public static UserDetail createDetails2(User user) {
    return UserDetail.builder()
                     .user(user)
                     .mbti(INFJ)
                     .smoke(NO_SMOKE)
                     .temperature(20)
                     .belief(BUDDHISM)
                     .oppositeFriends(NO)
                     .drink(Drink.ENJOY)
                     .height(173)
                     .job(PART_TIME)
                     .build();
  }

  public static UserDetail createDetails3(User user) {
    return UserDetail.builder()
                     .user(user)
                     .drink(Drink.ENJOY)
                     .height(173)
                     .temperature(10)
                     .mbti(ISFJ)
                     .belief(ELSE)
                     .smoke(NO_SMOKE)
                     .oppositeFriends(A_FEW)
                     .job(STUDENT)
                     .build();
  }

  public static UserDetail createDetails4(User user) {
    return UserDetail.builder()
                     .user(user)
                     .mbti(ENFJ)
                     .smoke(SMOKER)
                     .temperature(10)
                     .belief(ELSE)
                     .oppositeFriends(A_FEW)
                     .drink(Drink.DRINKER)
                     .height(123)
                     .job(PART_TIME)
                     .build();
  }

  public static User createNonCertificationUser() {
    return User.builder()
               .socialId(1L)
               .email("a")
               .mainProfile("a")
               .partnerGender(PartnerGender.OTHER)
               .role(Role.USER)
               .introduce("a")
               .username("a")
               .code("123456")
               .certification(false)
               .build();
  }

  public static User createTestUser(int count) {
    int year = 2000 + count;
    return User.builder()
               .socialId(1L)
               .username("testuser" + count)
               .birth(LocalDate.of(year, 1, 1))
               .role(Role.USER)
               .gender(Gender.MALE)
               .email("testuser" + count + "@example.com")
               .introduce("나는 테스트 유저 " + count)
               .code("CODE" + count)
               .inputCode("INPUT" + count)
               .partnerGender(PartnerGender.OTHER)
               .phoneNumber("010-0000-0000")
               .certification(true)
               .address(Address.of("seoul", ""))
               .mainProfile("profile" + count + ".jpg")
               .build();
  }

  public static JoinUserRequest createJoinUserRequest(int index) {
    return new JoinUserRequest(
        (long) index,
        "user" + index + "@example.com",
        index % 2 == 0 ? "남성" : "여성",
        "유저" + index,
        "11",
        "서울특별시",
        "199" + index + "-01-01",
        "테스트 자기소개 " + index,
        "Q1_" + index,
        "Q2_" + index,
        "Q3_" + index,
        "0101234000" + index,
        "code" + index,
        index % 2 == 0 ? "이성" : "동성",
        "fcm_token_" + index
    );
  }

  public static JoinDetailsRequest createJoinDetailsRequest(int index) {
    return new JoinDetailsRequest(
        170 + index,                            // height
        index % 2 == 0 ? "가끔 마셔요" : "애주가",   // drink
        index % 2 == 0 ? "비흡연" : "흡연",         // smoke
        "무교",                                  // belief
        index % 2 == 0 ? "많이 있어요" : "아예 없어요",          // oppositeFriends
        "직장인",                                // job
        "ISFJ",                                 // mbti
        50 + index                              // temperature
    );
  }

  public static MultipartFile createMultipartFile(int index) {
    return new MockMultipartFile(
        "mainProfile",
        "main_profile_" + index + ".jpg",
        "image/jpeg",
        ("main-profile-content-" + index).getBytes(StandardCharsets.UTF_8)
    );
  }

  public static List<MultipartFile> createMultipartFiles(int count) {
    return IntStream.range(0, count)
                    .mapToObj(i -> new MockMultipartFile(
                        "file" + i,
                        "profile_" + i + ".jpg",
                        "image/jpeg",
                        ("profile-file-content-" + i).getBytes(StandardCharsets.UTF_8)
                    ))
                    .collect(Collectors.toList());
  }

  public static OAuthLoginRequest createOAuthLoginRequest() {
    return new OAuthLoginRequest("accessToken");
  }

  public static KakaoDto createKakaoDto() {
    return new KakaoDto(1L);
  }

  public static Question createQuestion1(User user) {
    return Question.builder().question1("a").user(user).question2("a").question3("a").build();
  }

  public static Question createQuestion2(User user) {
    return Question.builder().question1("b").user(user).question2("b").question3("b").build();
  }

  public static Picture createPicture1(User user) {
    return Picture.builder().s3Path("a").originalFileName("a").user(user).build();
  }

  public static Picture createPicture2(User user) {
    return Picture.builder().s3Path("b").originalFileName("b").user(user).build();
  }

  public static Picture createPicture3(User user) {
    return Picture.builder().s3Path("c").originalFileName("c").user(user).build();
  }

  public static Card createCard(User user) {
    return Card.builder().profileUser(user).build();
  }

  public static Block createBlock(User blocker, User blockedUser) {
    return Block.builder().blockedUser(blockedUser).blocker(blocker).build();
  }

  public static RefreshToken createRefreshToken(User user) {
    return RefreshToken.builder().refreshToken("refreshToken").user(user).build();
  }
}
