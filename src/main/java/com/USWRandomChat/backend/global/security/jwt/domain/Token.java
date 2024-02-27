package com.USWRandomChat.backend.global.security.jwt.domain;

import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import javax.persistence.*;
import java.util.concurrent.TimeUnit;

@Getter
@RedisHash("refreshToken")
@Builder @AllArgsConstructor
public class Token {

    @Id
    private Long id;

    private String account;

    private String refreshToken;

    @TimeToLive(unit = TimeUnit.SECONDS)
    private long expiration;

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

