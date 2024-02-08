package com.USWRandomChat.backend.profile.dto;

import lombok.Data;

@Data
public class ProfileRequest {
    private String nickname;
    private String mbti;
    private String intro;
}
