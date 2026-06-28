package com.HomeRun.config;

import com.HomeRun.security.*;
import com.HomeRun.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtProvider jwtProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                // 💡 중요: 세션을 사용하지 않겠다(STATELESS)고 선언합니다. (JWT 방식의 핵심)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login**",
                                "/api/token-test",
                                "/api/auth/**",
                                "/v3/api-docs/**",     // Swagger 데이터
                                "/swagger-ui/**",      // Swagger UI 화면
                                "/swagger-ui.html"     // Swagger UI 진입점
                        ).permitAll() // 토큰 테스트 URL은 통과시켜 줍니다.
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        // 💡 중요: 로그인 성공 시 기본 URL로 가는 대신, 우리가 만든 핸들러가 작동하도록 설정합니다.
                        .successHandler(oAuth2SuccessHandler)
                )

                // 💡 인증 인가 실패 시 처리 핸들러 등록
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                // 💡 중요: 스프링 기본 인증 필터가 작동하기 전에, 우리가 만든 JwtFilter를 먼저 거치도록 설정합니다.
                .addFilterBefore(new JwtFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}