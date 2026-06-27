package com.HomeRun.dto.auth;

// import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailSendRequestDto {

    // @Schema(description = "인증번호를 받을 이메일 주소", example = "user@example.com")
    private String email;
}
