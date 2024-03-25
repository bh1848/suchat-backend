package com.USWRandomChat.backend.member.dto;

import lombok.Data;

@Data
public class SendVerificationCodeRequest {
    private String account;
    private String email;
}
