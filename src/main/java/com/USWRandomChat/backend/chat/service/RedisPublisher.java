package com.USWRandomChat.backend.chat.service;

import com.USWRandomChat.backend.chat.chatDTO.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPublisher {
    /*
    * RedisTopic에 저장-> 메시지 발행-> redis 구독 서비스가 메시지 처리
    * */
    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(ChannelTopic topic, ChatMessage message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}
