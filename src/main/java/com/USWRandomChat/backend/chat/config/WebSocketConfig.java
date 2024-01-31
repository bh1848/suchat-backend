package com.USWRandomChat.backend.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //웹소켓 생성 위치
        registry.addEndpoint("/stomp")
                .setAllowedOrigins("https://jxy.me");

    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        //메시지 발송(구독자-> 서버)
        config.enableSimpleBroker("/sub");
        //메시지 수신(브로커-> 구독자)
        config.setApplicationDestinationPrefixes("/pub");
    }
}
