package com.thred.datingapp.common.config.interceptor;

import com.thred.datingapp.common.error.CustomException;
import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import com.thred.datingapp.common.utils.JwtUtils;
import io.jsonwebtoken.lang.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtPrincipalHandshakeHandler extends DefaultHandshakeHandler {
  private final JwtUtils jwtUtils;
  @Override
  protected Principal determineUser(final ServerHttpRequest request, final WebSocketHandler wsHandler, final Map<String, Object> attributes) {

    String bearer = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    String token = jwtParse(bearer);
    String email = jwtUtils.getEmail(token);
    if (email == null || email.isBlank()) {
      log.error("[determineUser] email is Null");
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }
    log.debug("[determineUser] email: {}", email);
    return () -> email;
  }

  private static String jwtParse(String token) {
    if (Strings.hasText(token) && token.startsWith("Bearer ")) {
      return token.substring(7);
    }
    return null;
  }

}
