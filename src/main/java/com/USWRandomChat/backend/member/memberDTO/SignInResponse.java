package com.USWRandomChat.backend.member.memberDTO;

import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.security.jwt.JwtProvider;
import com.USWRandomChat.backend.security.jwt.dto.TokenDto;
import com.USWRandomChat.backend.security.jwt.service.JwtService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignInResponse {

    private String memberId;
    private String password;
    private TokenDto token;

    public SignInResponse(Member member, JwtProvider jwtProvider, JwtService jwtService) {
        this.memberId = member.getMemberId();
        this.password = member.getPassword();
        this.token = TokenDto.builder()
                .access_token(jwtProvider.createToken(member.getMemberId(), member.getRoles()))
                .refresh_token(jwtService.createRefreshToken(member))
                .build();
    }
}
