package com.USWRandomChat.backend.member.memberDTO;

import com.USWRandomChat.backend.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendRandomCodeResponse {
    private String account;
    private String email;
}
