package com.USWRandomChat.backend.memberDTO;

import com.USWRandomChat.backend.domain.Authority;
import com.USWRandomChat.backend.domain.Member;
import com.USWRandomChat.backend.security.jwt.Jwt;
import com.USWRandomChat.backend.security.jwt.JwtDto;
import com.USWRandomChat.backend.security.jwt.JwtProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignInResponse {

    private String memberId;
    private String memberPw;
    private JwtDto token;

    public SignInResponse(Member member, JwtProvider jwtProvider, Jwt jwt) {
        this.memberId = member.getMemberId();
        this.memberPw = member.getPassword();
        this.token = JwtDto.builder()
                .access_token(jwtProvider.createAccessToken(member.getMemberId(), member.getRoles()))
                .refresh_token(jwt.getRefreshToken())
                .build();
    }
}
