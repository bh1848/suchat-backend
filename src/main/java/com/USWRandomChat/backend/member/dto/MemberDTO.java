package com.USWRandomChat.backend.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberDTO {

    private String account;
    private String password;
    private String email;

    // Profile 관련 필드 추가
    private String nickname;
    private String mbti;
    private String intro;
}
