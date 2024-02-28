package com.USWRandomChat.backend.member.verification.dto;

import lombok.Data;

@Data
public class SendVerificationCodeRequest {
    private String account;
    private String email;
}
