package com.HomeRun.dto;

import lombok.Getter;
import lombok.Setter;

public class AuthDto {

    @Getter
    @Setter
    public static class SignupRequest {
        private String email;
        private String name;
        private String password;
    }

    @Getter
    @Setter
    public static class LoginRequest {
        private String email;
        private String password;
    }

}
