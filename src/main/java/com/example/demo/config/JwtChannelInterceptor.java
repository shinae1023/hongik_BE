package com.example.demo.config;

import com.example.demo.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.example.demo.security.UserInfo;

@Slf4j
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final TokenProvider tokenProvider;

    public JwtChannelInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);

                if (tokenProvider.validateToken(token)) {
                    Authentication authentication = tokenProvider.getAuthentication(token);

                    // ❗ [수정] Principal 객체 대신, 사용자 ID만 세션에 저장합니다.
                    UserInfo userInfo = (UserInfo) authentication.getPrincipal();
                    if (userInfo != null && userInfo.getUser() != null) {
                        accessor.getSessionAttributes().put("userId", userInfo.getUser().getUserId());
                        log.info("WebSocket 세션에 사용자 ID 저장 성공: {}", userInfo.getUser().getUserId());
                    } else {
                        log.error("인증 객체에서 사용자 정보를 찾을 수 없습니다.");
                        return null; // 또는 에러 처리
                    }
                    // accessor.setUser(authentication); // 이 줄은 더 이상 필요 없습니다.
                }
            }
        }
        return message;
    }
}