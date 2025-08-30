package com.thred.datingapp.user.controller;

import com.thred.datingapp.common.api.response.ApiDataResponse;
import com.thred.datingapp.common.utils.CookieUtils;
import com.thred.datingapp.common.utils.JwtUtils;
import com.thred.datingapp.user.api.request.OAuthLoginRequest;
import com.thred.datingapp.user.api.request.PhoneLoginRequest;
import com.thred.datingapp.user.dto.LoginDto;
import com.thred.datingapp.user.api.response.ProcessingResultResponse;
import com.thred.datingapp.common.annotation.Login;
import com.thred.datingapp.user.api.response.LoginResponse;
import com.thred.datingapp.user.dto.Tokens;
import com.thred.datingapp.user.service.LoginService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.*;

import static com.thred.datingapp.user.properties.JwtProperties.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final JwtUtils    jwtUtils;
    private final CookieUtils cookieUtils;

    @PostMapping("/login/reissue")
    public ApiDataResponse<ProcessingResultResponse> reissue(HttpServletResponse response, @CookieValue(name="refresh",required = false) String refresh){
        log.info("[API CALL] /api/login/reissue - 토큰 재발급 요청");
        log.debug("[reissue] refresh: {}", refresh);
        Tokens tokens = loginService.reissue(refresh);
        jwtUtils.addAccessToken(response,tokens.accessToken());
        cookieUtils.addCookie(response,REFRESH_TOKEN,tokens.refreshToken());
        return ApiDataResponse.ok(ProcessingResultResponse.from(true));
    }

    @PostMapping("/logout")
    public ApiDataResponse<ProcessingResultResponse> logout(@Login Long userId,HttpServletResponse response){
        log.info("[API CALL] /api/logout - 로그아웃 요청");
        log.debug("[logout] userId: {}", userId);
        loginService.logout(userId);
        cookieUtils.deleteCookie(response,REFRESH_TOKEN);
        return ApiDataResponse.ok(ProcessingResultResponse.from(true));
    }

    @PostMapping("/oauth/login")
    public ApiDataResponse<LoginResponse> oAuthLogin(@RequestBody OAuthLoginRequest oAuthLoginRequest, HttpServletResponse response){
        log.info("[API CALL] /api/oauth/login - OAuth 로그인 요청");
        log.debug("[oAuthLogin] oAuthLoginRequest: {}", oAuthLoginRequest);
        LoginDto loginDto = loginService.loginWithOAuth(oAuthLoginRequest);
        setLoginTokens(response, loginDto);
        return ApiDataResponse.ok(LoginResponse.of(loginDto.status(), loginDto.certification(), loginDto.role()));
    }

    @PostMapping("/phone/login")
    public ApiDataResponse<LoginResponse> phoneLogin(@RequestBody PhoneLoginRequest phoneLoginRequest, HttpServletResponse response) {
        log.info("[API CALL] /api/phone/login - 핸드폰 번호 로그인 요청");
        log.debug("[phoneLogin] oAuthLoginRequest: {}", phoneLoginRequest);
        LoginDto loginDto = loginService.phoneLogin(phoneLoginRequest);
        setLoginTokens(response, loginDto);
        return ApiDataResponse.ok(LoginResponse.of(loginDto.status(), loginDto.certification(), loginDto.role()));
    }

    private void setLoginTokens(final HttpServletResponse response, final LoginDto loginDto) {
        if(loginDto.status()) {
            if(Strings.isNotBlank(loginDto.accessToken())) {
                response.setHeader(HEADER_STRING, TOKEN_PREFIX + loginDto.accessToken());
                response.setHeader(USER_ID, loginDto.userId().toString());
            }
            if (Strings.isNotBlank(loginDto.refreshToken())) {
                cookieUtils.addCookie(response, REFRESH_TOKEN, loginDto.refreshToken());
            }
        }
    }
}
