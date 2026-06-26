package com.HomeRun.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {

    private String email;

    private String password;
}