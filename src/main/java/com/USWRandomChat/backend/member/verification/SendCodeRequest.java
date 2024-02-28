package com.USWRandomChat.backend.member.verification;

import lombok.Getter;

@Getter
public class SendCodeRequest {
    private String account;
    private String email;
}
