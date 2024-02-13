package com.USWRandomChat.backend.member.domain;

import com.USWRandomChat.backend.security.domain.Authority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "member_table")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 중복 x, id 역할
    @Column(nullable = false, updatable = false, unique = true)
    private String memberId;

    @Column(nullable = false)
    private String password;

    private boolean isEmailVerified;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String nickname;

    private LocalDateTime nicknameSetDate;

    private String mbti;

    private String intro;

    private String refreshToken;

    @OneToMany(mappedBy = "member", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Authority> roles = new ArrayList<>();

    public void setRoles(List<Authority> role) {
        this.roles = role;
        role.forEach(o -> o.setMember(this));
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setVerified() {
        this.isEmailVerified = true;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setNicknameSetDate(LocalDateTime nicknameSetDate){
        this.nicknameSetDate = nicknameSetDate;
    }

    public void setMbti(String mbti) {
        this.mbti = mbti;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

}