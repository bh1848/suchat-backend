package com.USWRandomChat.backend.member.domain;

import com.USWRandomChat.backend.security.domain.Authority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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
    private String account;

    @Column(nullable = false)
    private String password;

    private boolean isEmailVerified;

    @Column(unique = true, nullable = false)
    private String email;

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

    public void updatePassword(String newPassword){
        this.password = newPassword;
    }

    public void setVerified() {
        this.isEmailVerified = true;
    }
}