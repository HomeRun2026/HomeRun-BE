package com.HomeRun.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/user")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal OAuth2User principal) {
        // 인증된 사용자의 정보가 없다면 null을 반환하거나 에러 처리를 할 수 있어
        if (principal == null) {
            return null;
        }

        // 구글 로그인 성공 시, 구글 서버로부터 받은 사용자 정보(이름, 이메일 등)를 그대로 반환해
        return principal.getAttributes();
    }
}