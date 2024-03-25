package com.USWRandomChat.backend.member.dto;

import lombok.Data;

@Data
public class SignInRequest {

    private String account;
    private String password;
}
