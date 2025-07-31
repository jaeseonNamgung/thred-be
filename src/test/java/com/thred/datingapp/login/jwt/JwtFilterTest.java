package com.thred.datingapp.login.jwt;


import com.thred.datingapp.common.config.SecurityConfig;
import com.thred.datingapp.common.error.CustomException;

import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.common.utils.CookieUtils;
import com.thred.datingapp.common.utils.JwtUtils;
import com.thred.datingapp.user.service.PrincipalDetailsService;

import com.thred.datingapp.common.utils.RedisUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = JwtFilterTest.TestController.class)
@Import(SecurityConfig.class)
class JwtFilterTest {

    @Autowired
    private MockMvc mockmvc;

    @MockBean
    private JwtUtils    jwtUtils;
    @MockBean
    private CookieUtils cookieUtils;
    @MockBean
    private RedisUtils  redisUtils;
    @MockBean
    private AuthenticationEntryPoint authenticationEntryPoint;
    @MockBean
    private PrincipalDetailsService principalDetailsService;

    @TestConfiguration
    @RestController
    public static class TestController {

        @GetMapping("/api/ex")
        public String testEndpoint() {
            return "Test Endpoint";
        }
        @PostMapping({"/api/user/join",
                "/api/user/email",
                "/api/user/code",
                "/api/user/username",
                "/api/user/40/details",
                "/api/user/40/check",
                "/api/login/reissue"})
        public String test(){
            return "test";
        }
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("지정된 url은 이 필터를 거치면 안된다. -> 필터를 거쳐 실패하면 403 오류가 남, 안거치면 400")
    public void shouldNotFilter(String url) throws Exception {
        mockmvc.perform(post(url))
                .andExpect(status().isOk());
    }

    static Stream<String> shouldNotFilter(){
        return Stream.of(
                "/api/user/join",
                "/api/user/email",
                "/api/user/code",
                "/api/user/username",
                "/api/user/40/details",
                "/api/user/40/check",
                "/api/login/reissue"
        );
    }

    @Test
    @DisplayName("그 외의 url은 필터를 거쳐야한다. -> 정상 응답")
    public void shouldFilterAndAuthenticateWithValidJwt1() throws Exception {
        // given
        String token = "TOKEN";
        when(jwtUtils.isExpired(token)).thenReturn(false);
        when(jwtUtils.getSocialId(token)).thenReturn(12345L);
        when(jwtUtils.getUserId(token)).thenReturn(1L);
        when(jwtUtils.getRole(token)).thenReturn("ROLE_USER");
        // when & then
        mockmvc.perform(get("/api/ex")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("그 외의 url은 필터를 거쳐야한다. -> 토큰이 만료된 경우")
    public void shouldFilterAndAuthenticateWithValidJwt2() throws Exception {
        String token = "TOKEN";
        when(jwtUtils.isExpired(anyString())).thenThrow(new CustomException(UserErrorCode.EXPIRED_TOKEN));


        mockmvc.perform(get("/api/ex")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

    }

}
