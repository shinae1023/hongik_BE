package com.example.demo.controller;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.MessageType;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final ChatService chatService;
    private final SimpMessageSendingOperations messagingTemplate;
    // ✅ 사용자 정보를 조회하기 위해 UserRepository를 주입받습니다.
    private final UserRepository userRepository;

    /**
     * 채팅 메시지 전송 및 저장
     * 클라이언트에서 /app/chat/{chatRoomId}/send 로 메시지 전송
     */
    @MessageMapping("/chat/{chatRoomId}/send")
    public void sendMessage(
            @DestinationVariable Long chatRoomId,
            @Payload ChatMessageRequest request,
            // ✅ @AuthenticationPrincipal 대신 SimpMessageHeaderAccessor를 사용합니다.
            SimpMessageHeaderAccessor headerAccessor) {

        // ✅ Interceptor에서 세션에 저장한 사용자 ID를 직접 가져옵니다.
        Long senderId = (Long) headerAccessor.getSessionAttributes().get("userId");

        // 사용자 ID가 없는 경우 에러 처리
        if (senderId == null) {
            log.error("메시지 전송 실패: 세션에서 사용자 ID를 찾을 수 없습니다.");
            // 필요하다면 이곳에서 클라이언트에게 에러 응답을 보낼 수 있습니다.
            return;
        }

        try {
            // ✅ 사용자 ID로 User 엔티티를 조회하여 닉네임 등 필요한 정보를 가져옵니다.
            User sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + senderId));
            String senderNickname = sender.getNickname();

            log.info("Message received: chatRoomId={}, senderId={}, message={}", chatRoomId, senderId, request.getMessage());

            // 메시지 저장 로직 호출
            ChatMessage savedMessage = chatService.sendMessage(
                    chatRoomId,
                    senderId,
                    request.getMessage()
            );

            log.info("메시지 저장 완료. MessageId: {}", savedMessage.getId());

            // 채팅방 모든 구독자에게 전송할 응답 DTO 생성
            ChatMessageResponse response = ChatMessageResponse.builder()
                    .messageId(savedMessage.getId())
                    .chatRoomId(chatRoomId)
                    .senderId(senderId)
                    .senderNickname(senderNickname) // 조회한 닉네임 사용
                    .message(savedMessage.getMessage())
                    .createdAt(savedMessage.getCreatedAt())
                    .build();

            // 해당 채팅방을 구독하는 모든 클라이언트에게 메시지 브로드캐스팅
            messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId, response);
            log.info("메시지 전송 완료. Destination: /topic/chat/{}", chatRoomId);

        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
            // 에러 발생 시, 요청을 보낸 사용자에게만 에러 메시지 전송
            // 사용자 특정 에러 전송을 위해서는 StompHeaderAccessor에서 user 정보를 가져와야 합니다.
            // 이 부분은 필요시 추가 구현이 필요할 수 있습니다.
        }
    }

    /**
     * 채팅방 입장 알림 및 처리 (예: 안읽은 메시지 읽음 처리)
     * 클라이언트에서 /app/chat/{chatRoomId}/join 로 입장 메시지 전송
     */
    @MessageMapping("/chat/{chatRoomId}/join")
    public void joinChatRoom(
            @DestinationVariable Long chatRoomId,
            // ✅ @AuthenticationPrincipal 대신 SimpMessageHeaderAccessor를 사용합니다.
            SimpMessageHeaderAccessor headerAccessor) {

        // ✅ 세션에서 사용자 ID를 가져옵니다.
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");

        if (userId == null) {
            log.error("채팅방 입장 실패: 세션에서 사용자 ID를 찾을 수 없습니다.");
            return;
        }

        try {
            // ✅ 사용자 ID로 User 엔티티를 조회하여 닉네임을 가져옵니다.
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));
            String nickname = user.getNickname();

            log.info("User joined: chatRoomId={}, userId={}, nickname={}", chatRoomId, userId, nickname);

            // 입장 시 안읽은 메시지 읽음 처리 등의 비즈니스 로직
            chatService.markMessagesAsRead(chatRoomId, userId);

            // 다른 사용자들에게 보낼 입장 알림 메시지 생성
            JoinNotificationResponse notification = JoinNotificationResponse.builder()
                    .userId(userId)
                    .nickname(nickname)
                    .message(nickname + "님이 채팅방에 입장했습니다.")
                    .build();

            // 해당 채팅방의 알림 토픽으로 입장 알림 브로드캐스팅
            messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId + "/notifications", notification);

        } catch (Exception e) {
            log.error("Error joining chat room: {}", e.getMessage(), e);
        }
    }

    /**
     * 단순 테스트용 메시지 핸들러
     */
    @MessageMapping("/test")
    public void testMessage(@Payload String message) {
        log.info("테스트 메시지 수신: {}", message);
        messagingTemplate.convertAndSend("/topic/test", "Echo: " + message);
    }

    // --- DTOs ---
    // (DTO는 변경사항 없음)

    @lombok.Getter
    @lombok.Setter
    public static class ChatMessageRequest {
        private String message;
    }

    @lombok.Getter
    @lombok.Builder
    public static class ChatMessageResponse {
        private Long messageId;
        private Long chatRoomId;
        private Long senderId;
        private String senderNickname;

        // --- ★ DTO 필드 수정 ★ ---
        private MessageType messageType; // 메시지 타입 추가
        private String message; // 텍스트 내용 또는 "사진"
        private List<String> imageUrls; // 이미지 URL 리스트 추가

        private java.time.LocalDateTime createdAt;

        // ChatMessage 엔티티를 DTO로 변환하는 정적 팩토리 메소드
        public static ChatMessageResponse from(ChatMessage message) {
            return ChatMessageResponse.builder()
                    .messageId(message.getId())
                    .chatRoomId(message.getChatRoom().getId())
                    .senderId(message.getSender().getUserId())
                    .senderNickname(message.getSender().getNickname())
                    .messageType(message.getMessageType())
                    .message(message.getMessage())
                    .imageUrls(message.getImageUrls()) // 이미지 URL 리스트 매핑
                    .createdAt(message.getCreatedAt())
                    .build();
        }
    }

    @lombok.Getter
    @lombok.Builder
    public static class JoinNotificationResponse {
        private Long userId;
        private String nickname;
        private String message;
    }

    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class ChatMessageErrorResponse {
        private String error;
    }
}
