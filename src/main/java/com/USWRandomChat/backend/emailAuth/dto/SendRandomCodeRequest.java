package com.USWRandomChat.backend.emailAuth.dto;

import lombok.Data;

@Data
public class SendRandomCodeRequest {
    private String memberId;
    private String email;
}
