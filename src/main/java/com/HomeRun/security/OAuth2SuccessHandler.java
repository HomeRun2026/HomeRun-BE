package com.HomeRun.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 1. 구글 로그인에 성공한 사용자 정보 가져오기
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // 2. 해당 사용자의 이메일로 JWT 생성
        String token = jwtProvider.createToken(email, "ROLE_USER");

        // 3. 토큰을 가지고 우리가 원하는 엔드포인트(혹은 앱의 스킴)로 리다이렉트
        // 임시로 토큰을 확인할 수 있도록 URL 파라미터에 붙여서 보냄
        String targetUrl = "/api/token-test?token=" + token;

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}