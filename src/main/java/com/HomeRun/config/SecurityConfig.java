package com.HomeRun.config;

import com.HomeRun.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 보호 비활성화 (API 서버의 경우 보통 비활성화함)
                .csrf(csrf -> csrf.disable())

                // 2. HTTP 요청에 대한 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login**").permitAll() // 메인 화면이나 로그인 관련 경로는 누구나 접근 가능
                        .anyRequest().authenticated()                 // 그 외의 모든 요청은 로그인한 사용자만 접근 가능
                )

                // 3. OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .defaultSuccessUrl("/api/user", true)         // 구글 로그인 성공 시 이동할 기본 경로
                );

        return http.build();
    }
}