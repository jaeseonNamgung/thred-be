package com.thred.datingapp.login.controller;

import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.utils.CookieUtils;
import com.thred.datingapp.common.utils.JwtUtils;
import com.thred.datingapp.user.controller.LoginController;
import com.thred.datingapp.user.dto.Tokens;
import com.thred.datingapp.user.service.LoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.thred.datingapp.common.error.errorCode.UserErrorCode.*;
import static com.thred.datingapp.user.properties.JwtProperties.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private LoginService loginService;
    @MockBean
    private JwtUtils    jwtUtils;
    @MockBean
    private CookieUtils cookieUtils;

    @Test
    @DisplayName("accessToken 재발급 실패 - refresh 토큰이 없는 경우 NO_REFRESH_TOKEN 예외가 터져야한다.")
    void noRefreshToken() throws Exception {
        // given
        given(loginService.reissue(null)).willThrow(new CustomException(MISSING_REFRESH_TOKEN));
        // when & then
        mockMvc.perform(post("/api/login/reissue"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("REFRESH 토큰이 없습니다."));

    }

    @Test
    @DisplayName("accessToken 재발급 실패 - refresh 토큰 값이 이상하면 INVALID_TOKEN 예외가 터져야한다.")
    void invalidRefreshToken() throws Exception {
        // given
        given(loginService.reissue("asd")).willThrow(new CustomException(INVALID_TOKEN));
        // when & then
        mockMvc.perform(post("/api/login/reissue")
                        .cookie(new Cookie("refresh","asd")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("토큰이 유효하지 않습니다."));

    }

    @Test
    @DisplayName("accessToken 재발급 실패 - refresh 토큰 값을 access 토큰으로 보내면 NOT_REFRESH_TOKEN 예외가 터져야한다.")
    void notRefreshToken() throws Exception {
        // given
        given(loginService.reissue("asd")).willThrow(new CustomException(NOT_REFRESH_TOKEN));
        // when & then
        mockMvc.perform(post("/api/login/reissue")
                        .cookie(new Cookie("refresh","asd")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("REFRESH 토큰이 아닙니다."));

    }

    @Test
    @DisplayName("accessToken 재발급 실패 - 만료된 토큰을 보내면 EXPIRED_TOKEN 예외가 터져야한다.")
    void expiredRefreshToken() throws Exception {
        // given
        given(loginService.reissue("asd")).willThrow(new CustomException(EXPIRED_TOKEN));
        // when & then
        mockMvc.perform(post("/api/login/reissue")
                        .cookie(new Cookie("refresh","asd")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("토큰이 만료되었습니다."));

    }

    @Test
    @DisplayName("accessToken 재발급 실패 - 레디스에 저장된 값이랑 다른 토큰이 오면 EXPIRED_TOKEN 예외가 터져야한다.")
    void expiredRefreshToken2() throws Exception {
        // given
        given(loginService.reissue("asd")).willThrow(new CustomException(EXPIRED_TOKEN));
        // when & then
        mockMvc.perform(post("/api/login/reissue")
                        .cookie(new Cookie("refresh","asd")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("토큰이 만료되었습니다."));

    }

    @Test
    @DisplayName("accessToken 재발급 성공 - 해더에 accessToken 쿠키에 새로운 refreshToken이 들어가있어야한다.")
    void successToken() throws Exception {
        // given
        String beforeRefreshToken="adsakldjalksdjsa";
        String newRefreshToken="sdflasjflksajflkajfsjd";
        String newAccessToken="asdsakdljasdklasjd";
        doAnswer(invocation->{
            HttpServletResponse response=invocation.getArgument(0);
            String accessToken=invocation.getArgument(1);
            response.setHeader(HEADER_STRING,TOKEN_PREFIX+accessToken);
            return null;
        }).when(jwtUtils).addAccessToken(any(HttpServletResponse.class),eq(newAccessToken));
        doAnswer(invocation->{
            HttpServletResponse response=invocation.getArgument(0);
            String key=invocation.getArgument(1);
            String value=invocation.getArgument(2);
            response.addCookie(new Cookie(key,value));
            return null;
        }).when(cookieUtils).addCookie(any(HttpServletResponse.class),eq(REFRESH_TOKEN),eq(newRefreshToken));
        given(loginService.reissue(beforeRefreshToken)).willReturn(new Tokens(newAccessToken,newRefreshToken));
        // when & then
        mockMvc.perform(post("/api/login/reissue")
                .cookie(new Cookie(REFRESH_TOKEN,beforeRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HEADER_STRING))
                .andExpect(cookie().exists(REFRESH_TOKEN));
    }
}
