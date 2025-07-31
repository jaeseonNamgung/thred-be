package com.thred.datingapp.user.controller;

import com.thred.datingapp.common.api.response.ApiDataResponse;
import com.thred.datingapp.common.utils.CookieUtils;
import com.thred.datingapp.common.utils.JwtUtils;
import com.thred.datingapp.user.api.request.OAuthLoginRequest;
import com.thred.datingapp.user.api.response.OAuthLoginResponse;
import com.thred.datingapp.user.api.response.ProcessingResultResponse;
import com.thred.datingapp.user.argumentResolver.Login;
import com.thred.datingapp.user.api.response.LoginResponse;
import com.thred.datingapp.user.dto.Tokens;
import com.thred.datingapp.user.properties.JwtProperties;
import com.thred.datingapp.user.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.*;

import static com.thred.datingapp.user.properties.JwtProperties.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final JwtUtils    jwtUtils;
    private final CookieUtils cookieUtils;

    @PostMapping("/login/reissue")
    public ApiDataResponse<ProcessingResultResponse> reissue(HttpServletResponse response, @CookieValue(name="refresh",required = false) String refresh){
        Tokens tokens = loginService.reissue(refresh);
        jwtUtils.addAccessToken(response,tokens.accessToken());
        cookieUtils.addCookie(response,REFRESH_TOKEN,tokens.refreshToken());
        return ApiDataResponse.ok(ProcessingResultResponse.from(true));
    }

    @PostMapping("/logout")
    public ApiDataResponse<ProcessingResultResponse> logout(@Login Long userId,HttpServletResponse response){
        loginService.logout(userId);
        cookieUtils.deleteCookie(response,REFRESH_TOKEN);
        return ApiDataResponse.ok(ProcessingResultResponse.from(true));
    }

    @PostMapping("/login")
    public ApiDataResponse<LoginResponse> login(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        String refreshToken = (String) request.getAttribute(REFRESH_TOKEN);
        loginService.saveRefreshToken(userId,refreshToken);
        return ApiDataResponse.ok(loginService.getLoginResponse(userId));
    }

    @PostMapping("/oauth/login")
    public ApiDataResponse<LoginResponse> oAuthLogin(@RequestBody OAuthLoginRequest oAuthLoginRequest, HttpServletResponse response){
        OAuthLoginResponse oAuthLoginResponse = loginService.loginWithOAuth(oAuthLoginRequest);
        if(oAuthLoginResponse.status() && oAuthLoginResponse.certification()) {
            if(Strings.isNotBlank(oAuthLoginResponse.accessToken())) {
                response.setHeader(HEADER_STRING, oAuthLoginResponse.accessToken());
                response.setHeader(USER_ID, oAuthLoginResponse.userId().toString());
            }
            if (Strings.isNotBlank(oAuthLoginResponse.refreshToken())) {
                cookieUtils.addCookie(response, REFRESH_TOKEN, oAuthLoginResponse.refreshToken());
            }
        }
        return ApiDataResponse.ok(LoginResponse.of(oAuthLoginResponse.status(), oAuthLoginResponse.certification(), oAuthLoginResponse.role()));
    }

}
