package com.USWRandomChat.backend.global.security.jwt.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
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

    @Setter
    private String refreshToken;

    @Setter
    @TimeToLive(unit = TimeUnit.SECONDS)
    private long expiration;

    private Token(String account, String refreshToken, long expiration) {
        this.account = account;
        this.refreshToken = refreshToken;
        this.expiration = expiration;
    }

    // 정적 팩토리 메소드
    public static Token createToken(String account, String refreshToken, long expirationInSeconds) {
        return new Token(account, refreshToken, expirationInSeconds);
    }

}

