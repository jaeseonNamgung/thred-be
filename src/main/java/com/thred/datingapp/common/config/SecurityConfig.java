package com.thred.datingapp.common.config;

import com.thred.datingapp.common.config.filter.CustomAccessDeniedHandler;
import com.thred.datingapp.common.config.filter.CustomAuthenticationEntryPoint;
import com.thred.datingapp.common.utils.JwtUtils;
import com.thred.datingapp.common.config.filter.ExceptionFilter;
import com.thred.datingapp.common.config.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    private final JwtUtils    jwtUtils;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.cors(cors->cors.configurationSource(corsConfigurationSource()));
        http.sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // formLogin을 비활성화 했으니 직접 등록
        http.authorizeHttpRequests(request->request
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/join","/api/login/reissue/**","/api/user/join/**","/api/user/email/**",
                        "/api/user/code/**","/api/user/username/**","/api/user/*/check","/api/login/**", "/api/oauth/login",
                        "/api-docs/**", "/swagger-ui/**", "/api/chat/room/withdraw/user/**",
                        "/api/community/withdraw/user/**", "/actuator/health").permitAll()
                .anyRequest().authenticated())
                .exceptionHandling(exception->{
                    // 인증 되지 않은 사용자가 보호된 리소스에 접근하려고 할때 예외 커스텀(토큰 자체가 없을때)
                    exception.authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                            .accessDeniedHandler(new CustomAccessDeniedHandler());
        });

        http.addFilterBefore(new JwtFilter(jwtUtils), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new ExceptionFilter(), JwtFilter.class);

        return http.build();
    }
    // cors 설정
    private CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setExposedHeaders(Collections.singletonList("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource
                = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", configuration);

        return urlBasedCorsConfigurationSource;
    }
}
