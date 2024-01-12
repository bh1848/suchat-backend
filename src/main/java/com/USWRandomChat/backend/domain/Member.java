package com.USWRandomChat.backend.domain;

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

    //중복 x, id 역할
    @Column(nullable = false, updatable = false, unique = true)
//    @Size(min = 4, max = 16, message = "아이디는 4자 이상 16자 이내로 작성해주세요")
//    @Pattern(regexp = "^[a-z0-9]*$", message = "아이디는 알파벳 소문자, 숫자만 사용 가능합니다.")
    private String memberId;

    @Column(nullable = false)
//    @Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이내로 작성해주세요")
//    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "비밀번호는 알파벳 대소문자, 숫자만 사용 가능합니다.")
    private String password;

    private boolean isEmailVerified;

    @Column(unique = true, nullable = false)
    private String email;

    private String nickname;

    private String mbti;

    private String intro;

    @OneToMany(mappedBy = "member", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Authority> roles = new ArrayList<>();

    public void setRoles(List<Authority> role) {
        this.roles = role;
        role.forEach(o -> o.setMember(this));
    }

    public void setVerified() {
        this.isEmailVerified = true;
    }
}