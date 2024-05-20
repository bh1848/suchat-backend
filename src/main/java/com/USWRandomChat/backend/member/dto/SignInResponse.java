package com.USWRandomChat.backend.member.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignInResponse {

    private String accessToken;
    private String refreshToken;

    public SignInResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}
