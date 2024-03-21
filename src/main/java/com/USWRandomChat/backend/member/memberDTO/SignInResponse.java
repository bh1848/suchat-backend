package com.USWRandomChat.backend.member.memberDTO;

import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import com.USWRandomChat.backend.global.security.jwt.service.JwtService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignInResponse {

    private String account;
    //비밀번호는 보안상 제외
    private String accessToken;

    public SignInResponse(Member member, JwtProvider jwtProvider) {
        this.account = member.getAccount();
        this.accessToken = jwtProvider.createAccessToken(member.getAccount(), member.getRoles());
    }
}
