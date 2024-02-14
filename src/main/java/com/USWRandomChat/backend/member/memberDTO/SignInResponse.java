package com.USWRandomChat.backend.member.memberDTO;

import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.security.jwt.JwtProvider;
import com.USWRandomChat.backend.security.jwt.dto.TokenDto;
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
    private String password;
    private TokenDto token;

    public SignInResponse(Member member, JwtProvider jwtProvider) {
        this.account = member.getAccount();
        this.password = member.getPassword();
        this.token = TokenDto.builder()
                .access_token(jwtProvider.createToken(member.getAccount(), member.getRoles()))
                .refresh_token(member.getRefreshToken())
                .build();
    }
}
