package com.USWRandomChat.backend.emailAuth.dto;

import com.USWRandomChat.backend.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeResponse {
    private String newPassword;
    private String confirmNewPassword;

    public PasswordChangeResponse(Member member){
        this.newPassword = member.getPassword();
        this.confirmNewPassword = member.getPassword();
    }

}
