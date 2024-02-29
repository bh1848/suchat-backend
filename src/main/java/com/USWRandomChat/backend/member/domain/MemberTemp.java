package com.USWRandomChat.backend.member.domain;

import com.USWRandomChat.backend.emailAuth.domain.EmailToken;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "member_temp_table")
public class MemberTemp {

    //이메일 인증 완료-> member_table

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
        임시 저장 속성 4가지
        account
        password
        email
        nickname
    */
    @Column(nullable = false, updatable = false, unique = true)
    private String account;

    @Setter
    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private String nickname;

    private LocalDateTime nicknameChangeDate;

    private boolean isEmailVerified;

    @OneToOne(mappedBy = "memberTemp", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private EmailToken emailToken;

    public void setVerified() {
        this.isEmailVerified = true;
    }
    
}