package com.HomeRun.dto.auth;

// import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerifyRequestDto {

    // @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    // @Schema(description = "입력한 6자리 인증번호", example = "123456")
    private String code;
}
