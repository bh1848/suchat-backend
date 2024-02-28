package com.USWRandomChat.backend.member.verification.dto;

import lombok.Data;

@Data
public class UpdatePasswordRequest {
    private String newPassword;
    private String confirmNewPassword;
}
