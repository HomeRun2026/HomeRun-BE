package com.HomeRun.service;

import com.HomeRun.common.error.ErrorCode;
import com.HomeRun.common.exception.GlobalException;
import com.HomeRun.entity.EmailVerification;
import com.HomeRun.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Transactional
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailVerificationRepository emailVerificationRepository;

    // 인증번호 발송 및 DB 저장 로직
    public void sendVerificationCode(String toEmail) {

        String verificationCode = generateRandomCode(); // 6자리 난수 생성
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5); // 만료 시간 5분 설정

        // 기존에 인증 요청을 한 적이 있는지?
        Optional<EmailVerification> existingVerification = emailVerificationRepository.findByEmail(toEmail);

        if (existingVerification.isPresent()) { // DB에 열이 이미 존재한다면 코드와 만료 시간만 업데이트
            existingVerification.get().updateVerification(verificationCode, expirationTime);
        }
        else { // 처음 요청이라면 DB에 새로운 열 생성
            EmailVerification newVerification = EmailVerification.builder()
                    .email(toEmail)
                    .verificationCode(verificationCode)
                    .expirationTime(expirationTime)
                    .build();

            emailVerificationRepository.save(newVerification);
        }

        // 실제 이메일 전송
        sendEmail(toEmail, verificationCode);
    }

    // 인증번호 검증 로직
    public void verifyCode(String email, String inputCode) {
        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "인증 요청 내역이 없습니다."));

        // 시간 만료
        if (LocalDateTime.now().isAfter(verification.getExpirationTime())) {
            throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "인증번호가 만료되었습니다. 다시 요청해 주세요.");
        }

        // 인증번호 불일치
        if (!verification.getVerificationCode().equals(inputCode)) {
            throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE, "인증번호가 일치하지 않습니다.");
        }

        // 검증 성공 시 DB 상태 변경
        verification.verifySuccess();
    }

    // 코드(난수) 생성 유틸리티 메서드
    private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999 숫자

        return String.valueOf(code);
    }

    // 스프링 메일 발송 유틸리티 메서드
    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[HomeRun] 이메일 인증번호 안내");
        message.setText("안녕하세요.\nHomeRun 앱 이용을 위한 인증번호입니다.\n\n"
                + "인증번호: " + code + "\n\n"
                + "해당 인증번호는 5분간 유효합니다.");

        javaMailSender.send(message);
    }
}