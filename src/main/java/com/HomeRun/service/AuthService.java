package com.HomeRun.service;

import com.HomeRun.common.error.ErrorCode;
import com.HomeRun.common.exception.GlobalException;
import com.HomeRun.dto.auth.*;
import com.HomeRun.entity.*;
import com.HomeRun.repository.*;
import com.HomeRun.security.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화 도구
    private final JwtProvider jwtProvider;         // 토큰 생성 도구
    private final RefreshTokenRepository refreshTokenRepository;

    // 일반 회원가입
    public void signup(SignupRequestDto request) {
        // 이미 가입된 이메일 여부 확인
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "이미 사용 중인 이메일입니다.");
        }

        // 비밀번호와 비밀번호 확인 일치 검사
        if(!request.getPassword().equals(request.getPasswordConfirm())){
            throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 새로운 유저 객체 생성 및 DB 저장
        User newUser = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .role(Role.USER)
                .build();

        userRepository.save(newUser);
    }

    // 일반 로그인
    @Transactional
    public TokenResponseDto login(LoginRequestDto request) {
        // 이메일로 유저 찾기
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "가입되지 않은 이메일입니다."));

        // 비밀번호 일치 여부 확인 (입력받은 원문과 DB의 암호화된 문자열 비교)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호가 일치하지 않습니다.");
        }

        // 액세스, 리프레시 토큰 발급
        String accessToken = jwtProvider.createAccessToken(user.getEmail(), user.getRole().getKey());
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        // 리프레시 토큰 DB 저장 (이미 있다면 업데이트)
        saveOrUpdateRefreshToken(user.getEmail(), refreshToken);

        // 비밀번호가 맞다면 JWT 토큰을 발급하여 돌려줌
        return new TokenResponseDto(accessToken, refreshToken);
    }

    // 리프레시 토큰 저장 내부 로직
    private void saveOrUpdateRefreshToken(String email, String refreshToken) {
        refreshTokenRepository.findByEmail(email)
                .ifPresentOrElse(
                        token -> token.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(new RefreshToken(email, refreshToken))
                );
    }

    // 새로운 Access Token 재발급(Reissue) 로직
    public TokenResponseDto reissueToken(String refreshToken) {
        // 1. 전달받은 리프레시 토큰이 유효한지 검사
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new GlobalException(ErrorCode.HANDLE_ACCESS_DENIED, "Refresh Token이 유효하지 않거나 만료되었습니다.");
        }

        // 2. 토큰에서 이메일 추출 및 DB와 비교
        String email = jwtProvider.getEmailFromToken(refreshToken);
        RefreshToken savedToken = refreshTokenRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(ErrorCode.HANDLE_ACCESS_DENIED, "로그인 기록이 없습니다."));

        if (!savedToken.getToken().equals(refreshToken)) {
            throw new GlobalException(ErrorCode.HANDLE_ACCESS_DENIED, "토큰 정보가 일치하지 않습니다.");
        }

        // 3. 유저 권한 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "사용자를 찾을 수 없습니다."));

        // 4. 새로운 토큰들 발급 및 DB 업데이트 (토큰 로테이션)
        String newAccessToken = jwtProvider.createAccessToken(email, user.getRole().getKey());
        String newRefreshToken = jwtProvider.createRefreshToken(email);

        savedToken.updateToken(newRefreshToken);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }

    // 약관 동의 및 Role 승급 로직
    public TokenResponseDto processConsent(String email, ConsentRequestDto consentDto){

        // 사용자 email이 DB에 존재하지 않을 경우 예외 처리
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "사용자를 찾을 수 없습니다."));

        // 약관 동의 예외 처리
        if(!consentDto.isServiceTermsAgreement() || !consentDto.isPersonalInfoAgreement()){
            throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "필수 약관에 모두 동의해야 서비스 이용이 가능합니다.");
        }

        // 사용자가 이미 USER role 인 경우 예외 처리
        if(user.getRole() == Role.USER){
            throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "이미 정식 가입이 완료된 사용자입니다.");
        }

        // GUEST -> USER role 변경 후 DB 저장
        user.upgradeToUser();
        userRepository.save(user);

        String newAccessToken = jwtProvider.createAccessToken(user.getEmail(), Role.USER.getKey());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getEmail());

        saveOrUpdateRefreshToken(user.getEmail(), newRefreshToken);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }

    // 이메일 인증번호 확인 및 비밀번호 재설정
    public void resetPassword(PasswordResetRequestDto request){

        // 가입된 유저인지 확인
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "가입되지 않은 이메일입니다."));

        // 새 비밀번호와 비밀번호 확인이 일치하는지 검증
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 구글 연동 회원가입 유저는 password Column 이 null 임
        if (user.getPassword() == null) {
            throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "구글 연동으로 가입된 계정입니다. 구글 로그인을 이용해주세요.");
        }


        // 이메일 인증번호 검증
        // 검증에 실패하면 여기서 에러가 터지며 멈추고, 통과하면 다음 줄로 넘어감
        emailService.verifyCode(request.getEmail(), request.getCode());

        // 검증 성공시 request Dto 에서 newPassword 변수 추출하여 encode 후 User 메소드로 password 업데이트
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedPassword);
    }



}