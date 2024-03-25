package com.USWRandomChat.backend.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {

    private String account;
    private String password;
    private String email;
    private String nickname;
    //회원가입 정보 주입으로 넣어둠
    private Boolean isEmailVerified = false;
}
