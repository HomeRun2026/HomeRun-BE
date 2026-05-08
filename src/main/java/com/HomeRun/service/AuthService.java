package com.HomeRun.service;

import com.HomeRun.dto.AuthDto;
import com.HomeRun.entity.User;
import com.HomeRun.repository.UserRepository;
import com.HomeRun.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화 도구
    private final JwtProvider jwtProvider;         // 토큰 생성 도구

    // 일반 회원가입
    public void signupUser(AuthDto.SignupRequest request) {
        // 이미 가입된 이메일인지?
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 새로운 유저 객체 생성 및 DB 저장
        User newUser = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(encodedPassword)
                .role("ROLE_USER")
                .build();

        userRepository.save(newUser);
    }

    // 일반 로그인
    public String login(AuthDto.LoginRequest request) {
        // 이메일로 유저 찾기
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 비밀번호 일치 여부 확인 (입력받은 원문과 DB의 암호화된 문자열 비교)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호가 맞다면 JWT 토큰을 발급하여 돌려줌
        return jwtProvider.createToken(user.getEmail(), user.getRole());
    }
}