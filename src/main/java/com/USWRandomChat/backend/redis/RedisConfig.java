package com.USWRandomChat.backend.redis;

import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
@RequiredArgsConstructor
@EnableRedisRepositories
public class RedisConfig {

    //test
    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private int redisPort;
//    @Value("${spring.redis.password}")
//    private String redisPassword;

    // RedisConnectionFactory에 호스트와 포트 번호를 설정하고 빈으로 등록한다.
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisHost);
        redisStandaloneConfiguration.setPort(redisPort);
        //redisStandaloneConfiguration.setPassword(redisPassword);

        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    // match팀 redisTemplate
    @Bean
    public RedisTemplate<String, String> matchredisTemplate() {
        RedisTemplate<String, String> matchredisTemplate = new RedisTemplate<>();
        matchredisTemplate.setConnectionFactory(redisConnectionFactory());
        matchredisTemplate.setKeySerializer(new StringRedisSerializer());
        matchredisTemplate.setValueSerializer(new StringRedisSerializer());
        return matchredisTemplate;
    }

    // 인증번호 redisTemplate
    @Bean
    public RedisTemplate<String, String> verificationRedisTemplate() {
        RedisTemplate<String, String> verificationRedisTemplate = new RedisTemplate<>();
        verificationRedisTemplate.setConnectionFactory(redisConnectionFactory());
        verificationRedisTemplate.setKeySerializer(new StringRedisSerializer());
        verificationRedisTemplate.setValueSerializer(new StringRedisSerializer());
        return verificationRedisTemplate;
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext
                        .SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext
                        .SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));


        return RedisCacheManager
                .RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }
}




