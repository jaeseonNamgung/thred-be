package com.thred.datingapp.common.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thred.datingapp.common.api.response.ApiErrorResponse;
import com.thred.datingapp.common.error.ErrorCode;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ObjectMapper objectMapper = new ObjectMapper();
        ErrorCode errorCode = UserErrorCode.AUTHENTICATION_FAILED;
        log.error("[handle] 인증 오류 ===> errorCode={}", errorCode);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorCode.getHttpStatus().value());
        response.getWriter().write(objectMapper.writeValueAsString(ApiErrorResponse.of(false, errorCode)));
        response.getWriter().flush();
        response.getWriter().close();
    }
}
