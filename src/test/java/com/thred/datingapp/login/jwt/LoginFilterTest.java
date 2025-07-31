package com.thred.datingapp.login.jwt;

import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testFixture.UserFixture;
import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.user.api.request.LoginRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("testCode")
class LoginFilterTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    EntityManager em;

    ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void beforeEach() {
        given(passwordEncoder.encode(anyString())).willReturn("asd");
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
    }

    @Test
    @DisplayName("로그인 성공")
    void loginSuccessTest() throws Exception {
        // given
        User user = UserFixture.createCertificationUser1();
        em.persist(user);
        em.flush();
        em.clear();
        LoginRequest loginRequest = makeSuccessLoginRequest();
        // when & then
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(cookie().exists("refresh"));
    }

    private LoginRequest makeSuccessLoginRequest() {
        return new LoginRequest("a");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않은 이메일")
    void loginFailTest() throws Exception {
        // given
        LoginRequest loginRequest = makeFailLoginRequest();
        // when & then
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(false));
    }

    private LoginRequest makeFailLoginRequest() {
        return new LoginRequest("ind07162@naver.com");
    }
}
