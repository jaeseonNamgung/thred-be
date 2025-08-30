package com.thred.datingapp.user.service;

import com.thred.datingapp.common.entity.user.RefreshToken;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.entity.user.field.LoginType;
import com.thred.datingapp.common.entity.user.field.UserState;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.common.utils.JwtUtils;
import com.thred.datingapp.common.utils.PhoneNumberUtils;
import com.thred.datingapp.report.service.ReportService;
import com.thred.datingapp.user.api.request.OAuthLoginRequest;
import com.thred.datingapp.user.api.request.PhoneLoginRequest;
import com.thred.datingapp.user.dto.LoginDto;
import com.thred.datingapp.user.dto.KakaoDto;
import com.thred.datingapp.user.dto.Tokens;
import com.thred.datingapp.user.repository.RefreshTokenRepository;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.thred.datingapp.user.properties.JwtProperties.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoginService {

  private final JwtUtils               jwtUtils;
  private final RefreshTokenRepository refreshTokenRepository;
  private final ReportService          reportService;
  private final UserService            userService;
  private final OAuthService           oAuthService;

  @Transactional
  public LoginDto loginWithOAuth(final OAuthLoginRequest loginRequest) {
    String oauthAccessToken = loginRequest.accessToken();
    if (Strings.isBlank(oauthAccessToken)) {
      log.error("[loginWithOAuth] OAuth AccessToken is null or blank");
      throw new CustomException(UserErrorCode.INVALID_TOKEN);
    }

    KakaoDto kakaoDto = oAuthService.getKakaoUserInfo(oauthAccessToken);

    if (kakaoDto.id() == null) {
      log.error("[loginWithOAuth] Kakao ID is null");
      throw new CustomException(UserErrorCode.INVALID_SOCIAL_RESPONSE);
    }
    Optional<User> optionalUser = userService.getUserBySocialId(kakaoDto.id());
    return processLogin(optionalUser);
  }

  @Transactional
  public LoginDto phoneLogin(final PhoneLoginRequest phoneLoginRequest) {
    String formattedPhoneNumber = PhoneNumberUtils.toE164Format(phoneLoginRequest.phoneNumber());
    Optional<User> optionalUser = userService.getUserByPhoneNumberAndLoginType(formattedPhoneNumber, LoginType.PHONE_NUMBER);
    return processLogin(optionalUser);
  }

  public void logout(Long userId) {
    refreshTokenRepository.deleteByUserId(userId);
  }

  @Transactional
  public void saveRefreshToken(Long userId, String refreshToken) {

    if (refreshTokenRepository.findByUserId(userId).isPresent()) {
      RefreshToken existingToken = refreshTokenRepository.findByUserId(userId).get();
      existingToken.changeRefreshToken(refreshToken);
      log.info("[saveRefreshToken] 기존 RefreshToken 갱신 완료 ===> userId: {}", userId);
    }

    User user = userService.getUserById(userId);
    RefreshToken token = RefreshToken.builder().refreshToken(refreshToken).user(user).build();
    refreshTokenRepository.save(token);
    log.info("[saveRefreshToken] RefreshToken 저장 완료 ===> userId: {}", userId);
  }

  @Transactional
  public Tokens reissue(String refresh) {
    String newAccessToken = reissueAccessToken(refresh);
    String newRefreshToken = reissueRefreshToken(refresh);
    return new Tokens(newAccessToken, newRefreshToken);
  }

  // Refresh Token 을 이용한 AccessToken 재발급
  public String reissueAccessToken(String refresh) {
    checkRefreshToken(refresh);
    Long userId = jwtUtils.getUserId(refresh);
    String email = jwtUtils.getEmail(refresh);
    String role = jwtUtils.getRole(refresh);
    String jwtToken = jwtUtils.createJwt(ACCESS_TOKEN, userId, email, role, ACCESS_EXPIRATION_TIME);
    log.info("[reissueRefreshToken] AccessToken 재발급 완료 ===> userId: {}", userId);
    return jwtToken;
  }

  @Transactional
  public String reissueRefreshToken(String refresh) {
    checkRefreshToken(refresh);
    Long userId = jwtUtils.getUserId(refresh);
    String email = jwtUtils.getEmail(refresh);
    String role = jwtUtils.getRole(refresh);
    String newRefreshToken = jwtUtils.createJwt(REFRESH_TOKEN, userId, email, role, REFRESH_EXPIRATION_TIME);
    saveRefreshToken(userId, newRefreshToken);
    log.info("[reissueRefreshToken] RefreshToken 재발급 완료 ===> userId: {}", userId);
    return newRefreshToken;
  }

  private LoginDto processLogin(Optional<User> optionalUser) {
    if (optionalUser.isEmpty()) {
      log.info("[processLogin] User is Null (회원가입 필요)");
      return LoginDto.failure();
    }

    User user = optionalUser.get();
    String accessToken = jwtUtils.createJwt(ACCESS_TOKEN, user.getId(), user.getEmail(), user.getRole().getRole(), ACCESS_EXPIRATION_TIME);
    String refreshToken = jwtUtils.createJwt(REFRESH_TOKEN, user.getId(), user.getEmail(), user.getRole().getRole(), REFRESH_EXPIRATION_TIME);
    if (user.getCertification() == null || !user.getCertification()) {
      log.warn("[processLogin] 인증되지 않은 회원입니다. ===> userId: {}", user.getId());
      return LoginDto.authorizationFailure(user.getId(), user.getRole().getRole(), accessToken, refreshToken);
    }
    handleSuspendedUserState(user);
    // 탈퇴 유예 기간 내에 로그인한 경우, 회원 상태를 ACTIVE로 복구하고 탈퇴 요청 취소
    user.cancelWithdraw();
    log.info("[loginWithOAuth] getLoginResponse 프로세스 완료 (인증된 회원) ===> userId: {}", user.getId());
    return LoginDto.success(user.getId(), user.getRole().getRole(), accessToken, refreshToken);
  }

  private void handleSuspendedUserState(User user) {
    if (UserState.SUSPENDED.equals(user.getUserState())) {
      int totalRemainingDays = reportService.getTotalRemainingSuspensionDays(user.getId());
      if (totalRemainingDays <= 0) {
        log.info("[verifyOAuthUserLoginState] Suspended -> Active로 변경 ===>userName: {}, totalRemainingDays: {}", user.getUsername(),
                 totalRemainingDays);
        user.updateUserState(UserState.ACTIVE);
      } else {
        log.error("[verifyOAuthUserLoginState] 관리자로 인한 정지된 사용자 ===>userName: {}, totalRemainingDays: {}", user.getUsername(), totalRemainingDays);
        throw new CustomException(UserErrorCode.SUSPENDED_USER);
      }
    }
  }

  private void checkRefreshToken(String refresh) {
    if (refresh == null) {
      log.error("[checkRefreshToken] Refresh is null");
      throw new CustomException(UserErrorCode.MISSING_REFRESH_TOKEN);
    }
    try {
      if (!jwtUtils.getCategory(refresh).equals(REFRESH_TOKEN)) {
        log.error("[checkRefreshToken] JWT Category 불일치 ===> jwtCategory: {}", jwtUtils.getCategory(refresh));
        throw new CustomException(UserErrorCode.NOT_REFRESH_TOKEN);
      }
    } catch (SignatureException e) {
      log.error("[checkRefreshToken] 유효하지 않은 토큰입니다. ===> errorMessage: {}", e.getMessage());
      throw new CustomException(UserErrorCode.INVALID_TOKEN);
    }
    if (jwtUtils.isExpired(refresh)) {
      log.error("[checkRefreshToken] 만료된 토큰입니다.");
      throw new CustomException(UserErrorCode.EXPIRED_TOKEN);
    }
    if (!refreshTokenRepository.existsByRefreshToken(refresh)) {
      log.error("[checkRefreshToken] DB에 저장된 토큰이 없습니다. ===> refreshToken: {}", refresh);
      throw new CustomException(UserErrorCode.MISSING_REFRESH_TOKEN);
    }
  }
}
