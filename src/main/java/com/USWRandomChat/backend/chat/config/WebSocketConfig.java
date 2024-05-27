package com.USWRandomChat.backend.chat.config;

import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final ChannelInterceptor channelInterceptor;
    private final JwtProvider jwtProvider;

    /*
    환경: HttpServletRequest: 서블릿 기반의 전통적인 Spring MVC 애플리케이션에서 사용
         ServerHttpRequest: 비동기식/리액티브 애플리케이션에서 사용
    패키지: HttpServletRequest: 서블릿 API의 일부
           ServerHttpRequest: Spring Framework의 일부
    기능: HttpServletRequest: 서블릿 API의 전체 기능을 제공하며, 세션 관리, 쿠키, 요청 파라미터 접근 등이 가능
          ServerHttpRequest: 비동기식 처리에 최적화된 인터페이스를 제공
     */

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp")
                .setAllowedOrigins("*")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        //Handshake 요청에서 JWT 토큰을 추출하여 인증된 사용자로 설정
                        String accessToken = request.getHeaders().getFirst("Authorization");
                        if (accessToken != null && accessToken.startsWith("Bearer ")) {
                            accessToken = accessToken.substring(7);
                            //JWT 토큰을 검증하고 사용자 정보를 추출
                            String username = jwtProvider.getAccount(accessToken);
                            return new UsernamePasswordAuthenticationToken(username, null, List.of());
                        }
                        return null;
                    }
                });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/sub", "/queue/match");
        config.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor);
    }
}
