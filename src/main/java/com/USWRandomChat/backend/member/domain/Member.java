package com.USWRandomChat.backend.member.domain;

import com.USWRandomChat.backend.emailAuth.domain.EmailToken;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.global.security.domain.Authority;
import lombok.*;

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

    @Setter
    @Column(nullable = false)
    private String password;

//    private boolean isEmailVerified;

    @Column(unique = true, nullable = false)
    private String email;

//    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private EmailToken emailToken;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Profile profile;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Authority> roles = new ArrayList<>();

    public void setRoles(List<Authority> role) {
        this.roles = role;
        role.forEach(o -> o.setMember(this));
    }


//    public void setVerified() {
//        this.isEmailVerified = true;
//    }

}