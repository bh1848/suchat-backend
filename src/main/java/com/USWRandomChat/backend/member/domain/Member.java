package com.USWRandomChat.backend.member.domain;

import com.USWRandomChat.backend.global.security.domain.Authority;
import com.USWRandomChat.backend.profile.domain.Profile;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
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
    @Column(name="ACCOUNT", updatable = false, unique = true)
    private String account;

    @Setter
    @Column(name="PASSWORD")
    private String password;
    
    @Column(name="EMAIL", unique = true)
    private String email;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Profile profile;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Authority> roles = new ArrayList<>();

    public void setRoles(List<Authority> role) {
        this.roles = role;
        role.forEach(o -> o.setMember(this));
    }
}