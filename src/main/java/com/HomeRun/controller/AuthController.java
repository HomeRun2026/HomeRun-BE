package com.HomeRun.controller;

import com.HomeRun.dto.AuthDto;
import com.HomeRun.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/signup (회원가입)
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody AuthDto.SignupRequest request) {
        try {
            authService.signupUser(request);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            // 에러 발생 시 400 Bad Request 에러 메시지 반환
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST /api/auth/login (로그인)
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthDto.LoginRequest request) {
        try {
            // 로그인이 성공하면 만들어진 JWT 토큰을 반환
            String token = authService.login(request);
            return ResponseEntity.ok(token);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}