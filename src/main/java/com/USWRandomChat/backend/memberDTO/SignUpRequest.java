package com.USWRandomChat.backend.memberDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignUpRequest {

    private String memberId;
    private String password;
    private String email;
    private String nickname;
}
