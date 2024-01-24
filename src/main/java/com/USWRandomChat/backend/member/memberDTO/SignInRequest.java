package com.USWRandomChat.backend.member.memberDTO;

import lombok.Data;

@Data
public class SignInRequest {

    private String memberId;
    private String password;
}
