package com.USWRandomChat.backend.redis;

import com.USWRandomChat.backend.chat.service.RedisSubscriber;
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
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
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
    public RedisTemplate<String, String> matchRedisTemplate() {
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

    //-- 채팅팀
    /*
     * 단일 Topic 사용
     * */
    @Bean
    public ChannelTopic channelTopic() {
        return new ChannelTopic("chatroom");
    }

    /*
     * redis에 pub된 메시지 처리-> 리스너 설정
     * */
    @Bean
    public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory,
                                                              MessageListenerAdapter listenerAdapter,
                                                              ChannelTopic channelTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, channelTopic);
        return container;
    }

    //실제 메시지를 처리하는 sub 설정
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "sendMessage");
    }

    /*
     * 어플리케이션에서 사용할 redisTemplate 설정
     * */
    @Bean
    public RedisTemplate<String, Object> chatRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> chatRedisTemplate = new RedisTemplate<>();
        chatRedisTemplate.setConnectionFactory(connectionFactory);

        chatRedisTemplate.setKeySerializer(new StringRedisSerializer());
        chatRedisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
        return chatRedisTemplate;
    }
}