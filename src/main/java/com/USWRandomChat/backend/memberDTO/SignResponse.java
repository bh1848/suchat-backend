package com.USWRandomChat.backend.memberDTO;

import com.USWRandomChat.backend.domain.Authority;
import com.USWRandomChat.backend.domain.Member;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignResponse {

    private Long id;
    private String memberId;
    private String password;
    private String email;
    private String nickname;
    private List<Authority> roles = new ArrayList<>();
    private String token;

    public SignResponse(Member member) {
        this.id = member.getId();
        this.memberId = member.getMemberId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.roles = member.getRoles();
    }
}
