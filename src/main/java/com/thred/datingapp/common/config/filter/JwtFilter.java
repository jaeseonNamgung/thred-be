package com.thred.datingapp.common.config.filter;

import static com.thred.datingapp.user.properties.JwtProperties.HEADER_STRING;
import static com.thred.datingapp.user.properties.JwtProperties.TOKEN_PREFIX;

import com.thred.datingapp.common.entity.user.User;
import com.thred.datingapp.common.utils.JwtUtils;
import com.thred.datingapp.user.dto.PrincipalDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        AntPathMatcher pathMatcher = new AntPathMatcher();

        return pathMatcher.match("/api/user/join", path) ||
                pathMatcher.match("/api/user/email/**", path) ||
                pathMatcher.match("/api/user/code/**", path) ||
                pathMatcher.match("/api/user/username/**", path) ||
                pathMatcher.match("/api/user/*/check", path) ||
                pathMatcher.match("/api/login/**", path) ||
                pathMatcher.match("/api/join/**", path) ||
                pathMatcher.match("/api/admin/join/**", path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractTokenFromHeader(request.getHeader(HEADER_STRING));
        if (token != null && !jwtUtils.isExpired(token)) {

            Long socialId = jwtUtils.getSocialId(token);
            Long userId = jwtUtils.getUserId(token);
            String role = jwtUtils.getRole(token);
            User user = User.createUserForJwt(socialId, userId, role);
            PrincipalDetails principleDetails = new PrincipalDetails(user);
            Authentication authentication1 = new UsernamePasswordAuthenticationToken(principleDetails, null,
                    principleDetails.getAuthorities());
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(authentication1);
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromHeader(String authentication) {
        if (authentication == null || !authentication.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        return authentication.replace(TOKEN_PREFIX, "");
    }
}
