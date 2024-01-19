package com.USWRandomChat.backend.memberDTO;

import lombok.Data;

@Data
public class SignUpRequest {

    private String memberId;
    private String password;
    private String email;
    private String nickname;
}
