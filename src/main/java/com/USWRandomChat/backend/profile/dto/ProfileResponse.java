package com.USWRandomChat.backend.profile.dto;

import com.USWRandomChat.backend.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {


    private String nickname;
    private String mbti;
    private String intro;

    public ProfileResponse (Member member) {
        this.nickname = member.getNickname();
        this.mbti = member.getMbti();
        this.intro = member.getIntro();
    }
}
