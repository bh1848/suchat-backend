package com.USWRandomChat.backend.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/*
 * dev에서만 내장 레디스 실행
 * */
@Profile("dev")
//@Configuration
public class EmbeddedRedisConfig {

    /*
     * M1 이슈
     * */

    @Value("${spring.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void redisServer() {
        redisServer = new RedisServer((redisPort));
        redisServer.start();
    }

    @PreDestroy
    public void stropRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}
