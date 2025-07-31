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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ObjectMapper objectMapper = new ObjectMapper();

        ErrorCode errorCode = UserErrorCode.ACCESS_DENIED;

        log.error("[handle] 서비스 권한 오류 ===> errorCode: {}", errorCode);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorCode.getHttpStatus().value());
        response.getWriter().write(objectMapper.writeValueAsString(ApiErrorResponse.of(false, errorCode)));
        response.getWriter().flush();
        response.getWriter().close();
    }

}
