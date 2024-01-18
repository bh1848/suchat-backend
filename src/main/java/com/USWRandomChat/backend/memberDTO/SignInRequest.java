package com.USWRandomChat.backend.memberDTO;

import lombok.Data;

@Data
public class SignInRequest {

    private Long id;
    private String memberId;
    private String password;
    private String email;
    private String nickname;
}
