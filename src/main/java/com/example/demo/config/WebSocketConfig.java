package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired(required = false) // 선택적 주입으로 변경
    private JwtChannelInterceptor jwtChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커 설정
        config.enableSimpleBroker("/topic", "/queue"); // 구독 경로
        config.setApplicationDestinationPrefixes("/app"); // 클라이언트에서 메시지 발행 경로
        config.setUserDestinationPrefix("/user"); // 특정 사용자에게 메시지 전송 경로
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트 설정
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*"); // CORS 설정
                //.withSockJS(); // SockJS 지원 (WebSocket을 지원하지 않는 브라우저 대응)

        // SockJS 없이 순수 WebSocket 엔드포인트도 추가
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .setAllowedOrigins("https://jiangxy.github.io");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }
}
