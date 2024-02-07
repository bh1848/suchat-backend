package com.USWRandomChat.backend.profile.domain;

import com.USWRandomChat.backend.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String nickname;

    private String mbti;

    private String intro;
    public void update(String mbti, String intro, String nickname) {
        this.mbti = mbti;
        this.intro = intro;
        this.nickname = nickname;
    }
}
