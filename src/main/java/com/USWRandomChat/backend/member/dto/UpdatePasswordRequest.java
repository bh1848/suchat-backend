package com.USWRandomChat.backend.member.dto;

import lombok.Data;

@Data
public class UpdatePasswordRequest {
    private String newPassword;
    private String confirmNewPassword;
}
