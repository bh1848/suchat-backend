package com.USWRandomChat.backend.memberDTO;

import com.USWRandomChat.backend.domain.Authority;
import com.USWRandomChat.backend.domain.Member;
import com.USWRandomChat.backend.security.jwt.JwtDto;
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
public class SignResponse {

    private Long id;
    private String memberId;
    private String email;
    private String nickname;
    private List<Authority> roles = new ArrayList<>();
    private JwtDto token;

    public SignResponse(Member member) {
        this.id = member.getId();
        this.memberId = member.getMemberId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.roles = member.getRoles();
    }
}
