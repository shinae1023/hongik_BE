package com.example.demo.config;

import com.example.demo.config.jwt.TokenProvider;
import com.example.demo.security.UserInfo;
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

@Slf4j
@Component
@RequiredArgsConstructor // 생성자 주입을 위해 사용
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final TokenProvider tokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // STOMP CONNECT 요청일 때만 JWT 검증 및 사용자 정보 저장
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("Received CONNECT request. Authorization Header: {}", authorizationHeader); // 헤더 수신 확인 로그

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);

                if (tokenProvider.validateToken(token)) {
                    Authentication authentication = tokenProvider.getAuthentication(token);

                    // Principal에서 UserInfo를 가져옵니다.
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof UserInfo) {
                        UserInfo userInfo = (UserInfo) principal;

                        // UserInfo 내부의 User 객체와 ID를 확인합니다.
                        if (userInfo.getUser() != null && userInfo.getUser().getUserId() != null) {
                            Long userId = userInfo.getUser().getUserId();
                            // ✅ 세션 속성에 사용자 ID를 저장합니다. 이 부분이 가장 중요합니다.
                            accessor.getSessionAttributes().put("userId", userId);
                            log.info("Successfully stored userId in WebSocket session. userId: {}", userId);
                        } else {
                            log.error("Authentication failed: UserInfo or User object is null.");
                        }
                    } else {
                        log.error("Authentication failed: Principal is not an instance of UserInfo. Actual type: {}", principal.getClass().getName());
                    }
                } else {
                    log.warn("WebSocket JWT token validation failed.");
                }
            } else {
                log.warn("WebSocket Authorization header is missing or does not start with 'Bearer '.");
            }
        }
        return message;
    }
}