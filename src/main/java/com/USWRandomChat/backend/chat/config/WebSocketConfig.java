package com.USWRandomChat.backend.chat.config;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccessTokenException;
import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
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
    public void registerStompEndpoints(StompEndpointRegistry registry) throws AccessTokenException {
        registry.addEndpoint("/stomp")
                .setAllowedOrigins("*")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        //클라이언트로부터 엑세스 토큰 받음
                        String accessToken = request.getHeaders().getFirst("Authorization");
                        if (accessToken != null && accessToken.startsWith("Bearer ")) {
                            accessToken = accessToken.substring(7);
                            //엑세스 토큰 검증
                            if (jwtProvider.validateAccessToken(accessToken)) {
                                //엑세스 토큰에서 account 추출
                                String username = jwtProvider.getAccount(accessToken);
                                //principal 객체에 account 설정
                                return new UsernamePasswordAuthenticationToken(username, null, List.of());
                            }
                        }
                        //엑세스 토큰이 없거나 유효하지 않으면 null 반환
                        throw new AccessTokenException(ExceptionType.ACCESS_TOKEN_EXPIRED);
                    }
                });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/sub", "/queue/match");
        config.setApplicationDestinationPrefixes("/pub");
    }
}
