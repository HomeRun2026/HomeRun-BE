package com.HomeRun.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReissueRequestDto {

    private String refreshToken;
}