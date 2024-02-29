package com.USWRandomChat.backend.emailAuth.domain;

import com.USWRandomChat.backend.member.domain.Member;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

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

    // 이메일 토큰 만료 시간
    private static final long EMAIL_TOKEN_EXPIRATION_TIME_VALUE = 5L;

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36)
    private String uuid;

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

    private LocalDateTime expirationDate;

    private boolean expired;

    //토큰과 연관된 회원 id
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
//    private Member member;

    // 이메일 인증 토큰 생성
    public static EmailToken createEmailToken(Member member) {
        EmailToken emailToken = EmailToken.builder()
                .expirationDate(LocalDateTime.now().plusMinutes(EMAIL_TOKEN_EXPIRATION_TIME_VALUE))
                .expired(false)
//                .member(member)
                .build();
        return emailToken;
    }

    //필드 갱신
    public void updateExpiredToken(LocalDateTime expirationDate, boolean expired, Member member) {
        this.expirationDate = expirationDate;
        this.expired = expired;
//        this.member = member;
    }

    // 토큰 만료
    public void setTokenToUsed() {
        this.expired = true;
    }

    //uuid 회원의 Id
//    public Long getId() {
//        return member.getId();
//    }
}