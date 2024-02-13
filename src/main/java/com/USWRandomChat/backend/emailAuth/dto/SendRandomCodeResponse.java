package com.USWRandomChat.backend.emailAuth.dto;

import com.USWRandomChat.backend.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendRandomCodeResponse {
    private String memberId;
    private String email;
}
