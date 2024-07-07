package com.USWRandomChat.backend.member.domain;

import com.USWRandomChat.backend.email.domain.EmailToken;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
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
    @Column(name="ACCOUNT", updatable = false, unique = true)
    private String account;

    @Setter
    @Column(name="PASSWORD")
    private String password;

    @Column(name="EMAIL", unique = true)
    private String email;
    
    @Column(name="NICKNAME")
    private String nickname;

    @Column(name="NICKNAMECHANGEDATE")
    private LocalDateTime nicknameChangeDate;

    @Column(name="ISEMAILVERIFIED")
    private boolean isEmailVerified;

    @OneToOne(mappedBy = "memberTemp", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private EmailToken emailToken;
}