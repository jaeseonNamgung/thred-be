package com.thred.datingapp.common.utils;

import com.thred.datingapp.common.error.CustomException;

import com.thred.datingapp.common.error.errorCode.UserErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.thred.datingapp.user.properties.JwtProperties.*;

@Component
public class JwtUtils {

  private final SecretKey secretKey;

  public JwtUtils(@Value("${jwt.secret.key}") String secret) {
    this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
  }

  public Long getUserId(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(USER_ID, Long.class);
  }

  public String getEmail(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(EMAIL, String.class);
  }

  public String getCategory(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(CATEGORY, String.class);
  }

  public String getRole(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(ROLE, String.class);
  }

  public Boolean isExpired(String token) {
    try {
      return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    } catch (SecurityException | SignatureException | MalformedJwtException | IllegalArgumentException e) {
      throw new CustomException(UserErrorCode.INVALID_TOKEN);
    } catch (ExpiredJwtException e) {
      throw new CustomException(UserErrorCode.EXPIRED_TOKEN);
    } catch (UnsupportedJwtException e) {
      throw new CustomException(UserErrorCode.UNSUPPORTED_TOKEN);
    }
  }

  public void addAccessToken(HttpServletResponse response, String token) {
    response.setHeader(HEADER_STRING, TOKEN_PREFIX + token);
  }

  public String createJwt(String category, Long userId, String email, String role, Long expired) {
    return Jwts.builder()
               .claim(USER_ID, userId)
               .claim(EMAIL, email)
               .claim(CATEGORY, category)
               .claim(ROLE, role)
               .issuedAt(new Date(System.currentTimeMillis()))
               .expiration(new Date(System.currentTimeMillis() + expired))
               .signWith(secretKey)
               .compact();
  }
}
