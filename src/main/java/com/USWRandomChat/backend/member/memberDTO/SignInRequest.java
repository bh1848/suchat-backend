package com.USWRandomChat.backend.member.memberDTO;

import lombok.Data;

@Data
public class SignInRequest {

    private String account;
    private String password;
}
