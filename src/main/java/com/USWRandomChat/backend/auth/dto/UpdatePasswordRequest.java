package com.USWRandomChat.backend.auth.dto;

import lombok.Data;

@Data
public class UpdatePasswordRequest {
    private String newPassword;
    private String confirmNewPassword;
}
