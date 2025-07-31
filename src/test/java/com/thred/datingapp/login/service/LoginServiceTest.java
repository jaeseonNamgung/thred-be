package com.thred.datingapp.login.service;

import com.testFixture.UserFixture;
import com.thred.datingapp.common.entity.user.RefreshToken;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.common.utils.JwtUtils;
import com.thred.datingapp.user.dto.Tokens;
import com.thred.datingapp.user.repository.RefreshTokenRepository;
import com.thred.datingapp.user.service.LoginService;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import static com.thred.datingapp.user.properties.JwtProperties.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;


@SpringBootTest
@Transactional
@ActiveProfiles("testCode")
class LoginServiceTest {

    @MockBean
    JwtUtils   jwtUtils;
    @Autowired
    LoginService loginService;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("reissueAccessToken 메소드 검증 - 토큰을 안들고 온 경우 NO_REFRESH_TOKEN 예외가 터져야한다.")
    void reissueAccessTokenException1(){
        CustomException customException = assertThrows(CustomException.class, () -> loginService.reissueAccessToken(null));
        assertThat(customException.getErrorCode()).isEqualTo(UserErrorCode.MISSING_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("reissueAccessToken 메소드 검증 -  토큰 종류가 access 토큰이면 NOT_REFRESH_TOKEN 예외가 터져야한다.")
    void reissueAccessTokenException2(){
        //given
        given(jwtUtils.getCategory("asd")).willReturn(ACCESS_TOKEN);
        //when & then
        CustomException customException = assertThrows(CustomException.class, () -> loginService.reissueAccessToken("asd"));
        assertThat(customException.getErrorCode()).isEqualTo(UserErrorCode.NOT_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("reissueAccessToken 메소드 검증 - refresh 토큰이 만료된 경우 EXPIRED_TOKEN 예외가 터져야한다.")
    void reissueAccessTokenException3(){
        //given
        given(jwtUtils.getCategory("asd")).willReturn(REFRESH_TOKEN);
        given(jwtUtils.isExpired("asd")).willReturn(true);
        //when & then
        CustomException customException = assertThrows(CustomException.class, () -> loginService.reissueAccessToken("asd"));
        assertThat(customException.getErrorCode()).isEqualTo(UserErrorCode.EXPIRED_TOKEN);
    }

    @Test
    @DisplayName("reissueAccessToken 메소드 검증 - refresh 토큰이 맞지만 데이터베이스에 저장되어 있지 않으면 EXPIRED_TOKEN 예외가 터져야한다.")
    void reissueAccessTokenException4(){
        //given
        given(jwtUtils.getCategory("asd")).willReturn(REFRESH_TOKEN);
        given(jwtUtils.isExpired("asd")).willReturn(false);
        given(jwtUtils.getUserId("asd")).willReturn(1L);
        //when & then
        assertThatThrownBy(()->loginService.reissueAccessToken("asd"))
                .isInstanceOf(CustomException.class)
                .hasMessage(UserErrorCode.EXPIRED_TOKEN.getMessage());
    }

    @Test
    @DisplayName("reissueAccessToken 메소드 검증 - 정상 상황")
    void reissueAccessToken(){
        //given
        User user = UserFixture.createCertificationUser1();;
        em.persist(user);
        RefreshToken token = RefreshToken.builder().user(user).refreshToken("asd").build();
        em.persist(token);
        given(jwtUtils.getCategory("asd")).willReturn(REFRESH_TOKEN);
        given(jwtUtils.isExpired("asd")).willReturn(false);
        given(jwtUtils.getUserId("asd")).willReturn(1L);
        given(jwtUtils.getRole("asd")).willReturn("user");
        given(jwtUtils.getSocialId("asd")).willReturn(12345L);
        given(jwtUtils.getUserId("asd")).willReturn(1L);
        given(jwtUtils.getRole("asd")).willReturn("user");
        given(jwtUtils.createJwt(ACCESS_TOKEN,12345L,1L,"user",ACCESS_EXPIRATION_TIME)).willReturn("adsasdasdsa");
        //when & then
        String accessToken = loginService.reissueAccessToken("asd");
        assertThat(accessToken).isNotNull();
        assertThat(accessToken).isEqualTo("adsasdasdsa");
    }

    @Test
    @DisplayName("reissueRefreshToken 메소드 검증 - 토큰을 안들고 온 경우 NO_REFRESH_TOKEN 예외가 터져야한다.")
    void reissueRefreshTokenException1(){
        CustomException customException = assertThrows(CustomException.class, () -> loginService.reissueRefreshToken(null));
        assertThat(customException.getErrorCode()).isEqualTo(UserErrorCode.MISSING_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("reissueRefreshToken 메소드 검증 - 토큰 종류가 access 토큰이면 NOT_REFRESH_TOKEN 예외가 터져야한다.")
    void reissueRefreshTokenException2(){
        //given
        given(jwtUtils.getCategory("asd")).willReturn(ACCESS_TOKEN);
        //when & then
        CustomException customException = assertThrows(CustomException.class, () -> loginService.reissueRefreshToken("asd"));
        assertThat(customException.getErrorCode()).isEqualTo(UserErrorCode.NOT_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("reissueRefreshToken 메소드 검증 - refresh 토큰이 만료된 경우 EXPIRED_TOKEN 예외가 터져야한다.")
    void reissueRefreshTokenException3(){
        //given
        given(jwtUtils.getCategory("asd")).willReturn(REFRESH_TOKEN);
        given(jwtUtils.isExpired("asd")).willReturn(true);
        //when & then
        CustomException customException = assertThrows(CustomException.class, () -> loginService.reissueRefreshToken("asd"));
        assertThat(customException.getErrorCode()).isEqualTo(UserErrorCode.EXPIRED_TOKEN);
    }

    @Test
    @DisplayName("reissueRefreshToken 메소드 검증 - refresh 토큰이 이전에 발급했던 토큰이며 데이터베이스에 있지 않으면 EXPIRED_TOKEN 예외가 터져야한다.")
    void reissueRefreshTokenException4(){
        //given
        given(jwtUtils.getCategory("asd")).willReturn(REFRESH_TOKEN);
        given(jwtUtils.isExpired("asd")).willReturn(false);
        given(jwtUtils.getUserId("asd")).willReturn(1L);
        //when & then
        CustomException customException = assertThrows(CustomException.class, () -> loginService.reissueRefreshToken("asd"));
        assertThat(customException.getErrorCode()).isEqualTo(UserErrorCode.EXPIRED_TOKEN);
    }

    @Test
    @DisplayName("reissueRefreshToken 메소드 검증 - refresh 토큰이 이전에 발급했던 토큰이며 레디스에 저장되어 있는 값과 다르면 EXPIRED_TOKEN 예외가 터져야한다.")
    void reissueRefreshTokenException5(){
        //given
        User user = UserFixture.createCertificationUser1();
        em.persist(user);
        RefreshToken token = RefreshToken.builder().user(user).refreshToken("aaa").build();
        em.persist(token);
        given(jwtUtils.getCategory("asd")).willReturn(REFRESH_TOKEN);
        given(jwtUtils.isExpired("asd")).willReturn(false);
        given(jwtUtils.getUserId("asd")).willReturn(user.getId());
        //when & then
        CustomException customException = assertThrows(CustomException.class, () -> loginService.reissueRefreshToken("asd"));
        assertThat(customException.getErrorCode()).isEqualTo(UserErrorCode.EXPIRED_TOKEN);
    }

    @Test
    @DisplayName("reissueRefreshToken 메소드 검증 - 이상한 refresh 토큰을 들고오면 INVALID_TOKEN 예외가 터져야한다.")
    void reissueRefreshTokenException6(){
        //given
        given(jwtUtils.getCategory("asd")).willThrow(new SignatureException("Invalid JWT signature"));
        //when & then
        CustomException customException = assertThrows(CustomException.class, () -> loginService.reissueRefreshToken("asd"));
        assertThat(customException.getErrorCode()).isEqualTo(UserErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("reissueRefreshToken 메소드 검증 - 정상 상황")
    void reissueRefreshToken(){
        //given
        User user = UserFixture.createCertificationUser1();
        em.persist(user);
        RefreshToken token = RefreshToken.builder().user(user).refreshToken("asd").build();
        em.persist(token);
        given(jwtUtils.getCategory("asd")).willReturn(REFRESH_TOKEN);
        given(jwtUtils.isExpired("asd")).willReturn(false);
        given(jwtUtils.getUserId("asd")).willReturn(user.getId());
        given(jwtUtils.getRole("asd")).willReturn("user");
        given(jwtUtils.getSocialId("asd")).willReturn(12345L);
        given(jwtUtils.getRole("asd")).willReturn("user");
        given(jwtUtils.createJwt(REFRESH_TOKEN,12345L,user.getId(),"user",REFRESH_EXPIRATION_TIME)).willReturn("adsasdasdsa");
        //when & then
        assertThat(loginService.reissueRefreshToken("asd")).isNotNull();
        Optional<RefreshToken> refreshTokenOptional = refreshTokenRepository.findByUserId(user.getId());
        assertThat(refreshTokenOptional).isPresent();
        assertThat(refreshTokenOptional.get().getRefreshToken()).isEqualTo("adsasdasdsa");
    }

    @Test
    @DisplayName("reissue 메소드 검증 - 토큰을 안들고 온 경우 NO_REFRESH_TOKEN 예외가 터져야한다.")
    void reissueException1(){
        CustomException customException = assertThrows(CustomException.class, () -> loginService.reissue(null));
        assertThat(customException.getErrorCode()).isEqualTo(UserErrorCode.MISSING_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("reissue 메소드 검증 - 토큰 종류가 access 토큰이면 NOT_REFRESH_TOKEN 예외가 터져야한다.")
    void reissueException2(){
        //given
        given(jwtUtils.getCategory("asd")).willReturn(ACCESS_TOKEN);
        //when & then
        CustomException customException = assertThrows(CustomException.class, () -> loginService.reissue("asd"));
        assertThat(customException.getErrorCode()).isEqualTo(UserErrorCode.NOT_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("reissue 메소드 검증 - refresh 토큰이 만료된 경우 EXPIRED_TOKEN 예외가 터져야한다.")
    void reissueException3(){
        //given
        given(jwtUtils.getCategory("asd")).willReturn(REFRESH_TOKEN);
        given(jwtUtils.isExpired("asd")).willReturn(true);
        //when & then
        CustomException customException = assertThrows(CustomException.class, () -> loginService.reissue("asd"));
        assertThat(customException.getErrorCode()).isEqualTo(UserErrorCode.EXPIRED_TOKEN);
    }

    @Test
    @DisplayName("reissue 메소드 검증 - refresh 토큰이 이전에 발급했던 토큰이며 데이터베이스에 있지 않으면 EXPIRED_TOKEN 예외가 터져야한다.")
    void reissueException4(){
        //given
        given(jwtUtils.getCategory("asd")).willReturn(REFRESH_TOKEN);
        given(jwtUtils.isExpired("asd")).willReturn(false);
        given(jwtUtils.getUserId("asd")).willReturn(1L);
        //when & then
        CustomException customException = assertThrows(CustomException.class, () -> loginService.reissue("asd"));
        assertThat(customException.getErrorCode()).isEqualTo(UserErrorCode.EXPIRED_TOKEN);
    }

    @Test
    @DisplayName("reissue 메소드 검증 - refresh 토큰이 이전에 발급했던 토큰이며 레디스에 저장되어 있는 값과 다르면 EXPIRED_TOKEN 예외가 터져야한다.")
    void reissueException5(){
        //given
        User user = UserFixture.createCertificationUser1();
        em.persist(user);
        RefreshToken token = RefreshToken.builder().user(user).refreshToken("aaa").build();
        em.persist(token);
        given(jwtUtils.getCategory("asd")).willReturn(REFRESH_TOKEN);
        given(jwtUtils.isExpired("asd")).willReturn(false);
        given(jwtUtils.getUserId("asd")).willReturn(user.getId());
        //when & then
        CustomException customException = assertThrows(CustomException.class, () -> loginService.reissue("asd"));
        assertThat(customException.getErrorCode()).isEqualTo(UserErrorCode.EXPIRED_TOKEN);
    }

    @Test
    @DisplayName("reissue 메소드 검증 - 이상한 refresh 토큰을 들고오면 INVALID_TOKEN 예외가 터져야한다.")
    void reissueException6(){
        //given
        given(jwtUtils.getCategory("asd")).willThrow(new SignatureException("Invalid JWT signature"));
        //when & then
        CustomException customException = assertThrows(CustomException.class, () -> loginService.reissue("asd"));
        assertThat(customException.getErrorCode()).isEqualTo(UserErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("reissue - 정상적인 토큰인 경우 access, refresh 토큰 모두 발급된다.")
    void reissue(){
        User user = UserFixture.createCertificationUser1();
        em.persist(user);
        RefreshToken token = RefreshToken.builder().user(user).refreshToken("asd").build();
        em.persist(token);
        given(jwtUtils.getCategory("asd")).willReturn(REFRESH_TOKEN);
        given(jwtUtils.isExpired("asd")).willReturn(false);
        given(jwtUtils.getUserId("asd")).willReturn(user.getId());
        given(jwtUtils.getRole("asd")).willReturn("user");
        given(jwtUtils.getSocialId("asd")).willReturn(12345L);
        given(jwtUtils.getRole("asd")).willReturn("user");
        given(jwtUtils.createJwt(REFRESH_TOKEN,12345L,user.getId(),"user",REFRESH_EXPIRATION_TIME)).willReturn("bbbb");
        given(jwtUtils.createJwt(ACCESS_TOKEN,12345L,user.getId(),"user",ACCESS_EXPIRATION_TIME)).willReturn("aaaa");
        //when & then
        Tokens newTokens = loginService.reissue("asd");
        assertThat(newTokens.accessToken()).isEqualTo("aaaa");
        assertThat(newTokens.refreshToken()).isEqualTo("bbbb");
        Optional<RefreshToken> refreshTokenOptional = refreshTokenRepository.findByUserId(user.getId());
        assertThat(refreshTokenOptional).isPresent();
        assertThat(refreshTokenOptional.get().getRefreshToken()).isEqualTo("bbbb");
    }

    @Test
    @DisplayName("saveRefreshToken - 이미 데이터 베이스에 refresh 토큰이 있는 경우 교체한다.")
    void saveRefreshToken_alreadyHave(){
        // given
        User user = UserFixture.createCertificationUser1();
        em.persist(user);
        RefreshToken token = RefreshToken.builder().user(user).refreshToken("aaa").build();
        em.persist(token);
        // when
        loginService.saveRefreshToken(user.getId(),"bbb");
        // then
        Optional<RefreshToken> refreshTokenOptional = refreshTokenRepository.findByUserId(user.getId());
        assertThat(refreshTokenOptional).isPresent();
        assertThat(refreshTokenOptional.get().getRefreshToken()).isEqualTo("bbb");
    }

    @Test
    @DisplayName("saveRefreshToken - 이미 데이터 베이스에 refresh 토큰이 없는 경우 생성한다..")
    void saveRefreshToken_noExist(){
        // given
        User user = UserFixture.createCertificationUser1();
        em.persist(user);
        // when
        loginService.saveRefreshToken(user.getId(),"bbb");
        // then
        Optional<RefreshToken> refreshTokenOptional = refreshTokenRepository.findByUserId(user.getId());
        assertThat(refreshTokenOptional).isPresent();
        assertThat(refreshTokenOptional.get().getRefreshToken()).isEqualTo("bbb");
    }

    @Test
    @DisplayName("logout - 로그아웃 시 서버에 있는 리프래쉬 토큰을 삭제한다.")
    void logout(){
        // given
        User user = UserFixture.createCertificationUser1();
        em.persist(user);
        RefreshToken token = RefreshToken.builder().user(user).refreshToken("asd").build();
        em.persist(token);
        // when
        loginService.logout(user.getId());
        // then
        Optional<RefreshToken> refreshTokenOptional = refreshTokenRepository.findByUserId(user.getId());
        assertThat(refreshTokenOptional).isEmpty();
    }
}
