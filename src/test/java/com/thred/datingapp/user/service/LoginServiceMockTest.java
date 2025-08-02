package com.thred.datingapp.user.service;

import com.testFixture.UserFixture;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.field.Role;
import com.thred.datingapp.common.entity.user.field.UserState;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.common.utils.JwtUtils;
import com.thred.datingapp.report.service.ReportService;
import com.thred.datingapp.user.api.request.OAuthLoginRequest;
import com.thred.datingapp.user.api.response.OAuthLoginResponse;
import com.thred.datingapp.user.dto.KakaoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.DateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.DATE;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class LoginServiceMockTest {

  @Mock
  private UserService   userService;
  @Mock
  private ReportService reportService;
  @Mock
  private OAuthService  oAuthService;
  @Mock
  private JwtUtils      jwtUtils;

  @InjectMocks
  private LoginService sut;

  @Test
  @DisplayName("AccessToken이 null 일 경우 예외 발생")
  void userLogin_whenAccessTokenIsNullOrBlank_thenThrowsInvalidTokenError() {
    // given
    OAuthLoginRequest oAuthLoginRequest = new OAuthLoginRequest(null);
    // when
    CustomException exception = assertThrows(CustomException.class, () -> sut.loginWithOAuth(oAuthLoginRequest));
    // then
    assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.INVALID_TOKEN);
  }

  @Test
  @DisplayName("socialId가 null 일 경우 예외 발생")
  void userLogin_whenSocialIdIsNullOrBlank_thenThrowsInvalidTokenError() {
    // given
    OAuthLoginRequest oAuthLoginRequest = UserFixture.createOAuthLoginRequest();
    given(oAuthService.getKakaoUserInfo(anyString())).willReturn(new KakaoDto(null));

    // when
    CustomException exception = assertThrows(CustomException.class, () -> sut.loginWithOAuth(oAuthLoginRequest));
    // then
    assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.INVALID_SOCIAL_RESPONSE);
    then(oAuthService).should().getKakaoUserInfo(anyString());
  }

  @Test
  @DisplayName("user가 존재하지 않을 경우 ofFailure 메서드를 리턴")
  void userLogin_whenSocialIdIsNullOrBlank_thenReturnsOfFailureMethod() {
    // given
    OAuthLoginRequest oAuthLoginRequest = UserFixture.createOAuthLoginRequest();
    given(oAuthService.getKakaoUserInfo(anyString())).willReturn(UserFixture.createKakaoDto());
    given(userService.getUserBySocialId(anyLong())).willReturn(Optional.empty());

    // when
    OAuthLoginResponse oAuthLoginResponse = sut.loginWithOAuth(oAuthLoginRequest);
    // then
    assertThat(oAuthLoginResponse.status()).isFalse();
    assertThat(oAuthLoginResponse.certification()).isFalse();
    assertThat(oAuthLoginResponse.role()).isNull();
    then(oAuthService).should().getKakaoUserInfo(anyString());
    then(userService).should().getUserBySocialId(anyLong());
  }

  @Test
  @DisplayName("인증되지 않은 회원일 경우 ofAuthorizationFailure 메서드를 리턴")
  void userLogin_whenIsNotCertification_thenReturnsOfAuthorizationFailure() {
    // given
    OAuthLoginRequest oAuthLoginRequest = UserFixture.createOAuthLoginRequest();
    given(oAuthService.getKakaoUserInfo(anyString())).willReturn(UserFixture.createKakaoDto());
    given(userService.getUserBySocialId(anyLong())).willReturn(Optional.of(UserFixture.createNonCertificationUser()));

    // when
    OAuthLoginResponse oAuthLoginResponse = sut.loginWithOAuth(oAuthLoginRequest);
    // then
    assertThat(oAuthLoginResponse.status()).isTrue();
    assertThat(oAuthLoginResponse.certification()).isFalse();
    assertThat(oAuthLoginResponse.role()).isEqualTo(Role.USER.getRole());
    then(oAuthService).should().getKakaoUserInfo(anyString());
    then(userService).should().getUserBySocialId(anyLong());
  }

  @Test
  @DisplayName("User State가 SUSPENDED이고 totalRemainingDays가 0보다 클때 SUSPENDED_USER 에러 발생")
  void userLogin_whenUserStateIsSuspendedAndTotalRemainingDaysGreaterThanZero_thenThrowsSUSPENDED_USERError() {
    // given
    OAuthLoginRequest oAuthLoginRequest = UserFixture.createOAuthLoginRequest();
    User user = UserFixture.createCertificationUser1();
    ReflectionTestUtils.setField(user, "id", 1L);
    ReflectionTestUtils.setField(user, "userState", UserState.SUSPENDED);

    given(oAuthService.getKakaoUserInfo(anyString())).willReturn(UserFixture.createKakaoDto());
    given(userService.getUserBySocialId(anyLong())).willReturn(Optional.of(user));
    given(reportService.getTotalRemainingSuspensionDays(anyLong())).willReturn(5);

    // when
    CustomException exception = assertThrows(CustomException.class, () -> sut.loginWithOAuth(oAuthLoginRequest));
    // then
    assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.SUSPENDED_USER);
    then(oAuthService).should().getKakaoUserInfo(anyString());
    then(userService).should().getUserBySocialId(anyLong());
    then(reportService).should().getTotalRemainingSuspensionDays(anyLong());
  }

  @Test
  @DisplayName("로그인 정상 테스트 - User State가 SUSPENDED이고 totalRemainingDays가 0 이하일 때 User State를 ACTIVE로 변경")
  void userLogin_whenUserStateIsSuspendedAndTotalRemainingDaysLessThanOrEqualToZero_thenUpdateUserStateToActive() {
    // given
    OAuthLoginRequest oAuthLoginRequest = UserFixture.createOAuthLoginRequest();
    User user = UserFixture.createCertificationUser1();
    ReflectionTestUtils.setField(user, "id", 1L);
    ReflectionTestUtils.setField(user, "userState", UserState.SUSPENDED);
    given(oAuthService.getKakaoUserInfo(anyString())).willReturn(UserFixture.createKakaoDto());
    given(userService.getUserBySocialId(anyLong())).willReturn(Optional.of(user));
    given(reportService.getTotalRemainingSuspensionDays(anyLong())).willReturn(0);
    given(jwtUtils.createJwt(anyString(), anyLong(), anyLong(), anyString(), anyLong()))
        .willReturn("accessToken").willReturn("refreshToken");

    // when
    OAuthLoginResponse oAuthLoginResponse = sut.loginWithOAuth(oAuthLoginRequest);
    // then
    assertThat(oAuthLoginResponse.status()).isTrue();
    assertThat(oAuthLoginResponse.certification()).isTrue();
    assertThat(oAuthLoginResponse.role()).isEqualTo(Role.USER.getRole());
    assertThat(oAuthLoginResponse.accessToken()).isEqualTo("accessToken");
    assertThat(oAuthLoginResponse.refreshToken()).isEqualTo("refreshToken");
    then(oAuthService).should().getKakaoUserInfo(anyString());
    then(userService).should().getUserBySocialId(anyLong());
    then(reportService).should().getTotalRemainingSuspensionDays(anyLong());
    then(jwtUtils).should(times(2)).createJwt(anyString(), anyLong(), anyLong(), anyString(), anyLong());
  }

}
