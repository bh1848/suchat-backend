package com.USWRandomChat.backend.profile.domain;

import com.USWRandomChat.backend.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @JoinColumn(name = "member_id")
    private Member member;

    private String nickname;

    private LocalDateTime nicknameChangeDate;

    private String mbti;

    private String intro;

    private String roomId;

    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    public void setNicknameChangeDate(LocalDateTime nicknameChangeDate){
        this.nicknameChangeDate = nicknameChangeDate;
    }

    public void setMbti(String mbti){
        this.mbti = mbti;
    }

    public void setIntro(String intro){
        this.intro = intro;
    }

    public void setRoomId(String roomId){
        this.roomId = roomId;
    }
}

