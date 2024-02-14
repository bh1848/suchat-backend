package com.USWRandomChat.backend.profile.dto;

import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.profile.domain.Profile;
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

    public ProfileResponse (Profile profile) {
        this.nickname = profile.getNickname();
        this.mbti = profile.getMbti();
        this.intro = profile.getIntro();
    }
}
