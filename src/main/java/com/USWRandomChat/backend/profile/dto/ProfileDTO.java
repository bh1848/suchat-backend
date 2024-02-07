package com.USWRandomChat.backend.profile.dto;

import com.USWRandomChat.backend.profile.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDTO {

    private String nickname;

    private String mbti;

    private String intro;

    public static ProfileDTO fromEntity(Profile profile) {
        return new ProfileDTO(
                profile.getNickname(),
                profile.getMbti(),
                profile.getIntro()
        );
    }
}
