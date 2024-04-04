package com.USWRandomChat.backend.member.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Id;

@Getter
@RedisHash("VerificationCode")
@Builder
@AllArgsConstructor
public class VerificationCode {
    @Id
    private Long id;

    private String account;

    private String code;

}