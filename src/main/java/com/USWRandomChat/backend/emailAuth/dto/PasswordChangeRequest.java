package com.USWRandomChat.backend.emailAuth.dto;

import lombok.Data;

@Data
public class PasswordChangeRequest {

    private String newPassword;
    private String confirmNewPassword;
}
