package com.thred.datingapp.common.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thred.datingapp.common.api.response.ApiDataResponse;
import com.thred.datingapp.common.api.response.ApiStatusResponse;
import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.ErrorCode;
import com.thred.datingapp.common.error.errorCode.BaseErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class ExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (CustomException e) {
            log.error("[doFilterInternal] Filter custom exception ===> ErrorCode: {}", e.getErrorCode());
            sendErrorResponse(response, e.getErrorCode());
        } catch (Exception e) {
            log.error("[doFilterInternal] Filter internal exception ===> errorMessage: {}", e.getMessage());
            sendErrorResponse(response, BaseErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(ApiDataResponse.error(ApiStatusResponse.of(false), errorCode)));
        response.getWriter().flush();
        response.getWriter().close();
    }
}
