package com.USWRandomChat.backend.auth.dto;

import lombok.Data;

@Data
public class SendVerificationCodeRequest {
    private String account;
    private String email;
}
