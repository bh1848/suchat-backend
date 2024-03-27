package com.USWRandomChat.backend.profile.domain;

import com.USWRandomChat.backend.member.domain.Member;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "profile_table")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id",referencedColumnName = "id")
    private Member member;

    @Setter
    @Column(nullable = false)
    private String nickname;

    @Setter
    private LocalDateTime nicknameChangeDate;

    @Setter
    private String mbti;

    @Setter
    private String intro;

    @Setter
    private String roomId;
}