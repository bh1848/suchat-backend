package com.USWRandomChat.backend.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtDto {

    private String access_token;
    private String refresh_token;
}
