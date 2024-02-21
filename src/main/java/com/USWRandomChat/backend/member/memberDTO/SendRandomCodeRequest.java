package com.USWRandomChat.backend.member.memberDTO;

import lombok.Data;

@Data
public class SendRandomCodeRequest {
    private String account;
    private String email;
}
