package com.USWRandomChat.backend.memberDTO;

import lombok.Data;

@Data
public class SignRequest {

    private Long id;
    private String memberId;
    private String password;
    private String email;
    private String nickname;
}
