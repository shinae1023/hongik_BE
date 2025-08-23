package com.example.demo.controller;

import com.example.demo.dto.request.CreateChatRoomRequestDto;
import com.example.demo.dto.response.ChatRoomResponseDto;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.MessageType;
import com.example.demo.entity.User;
import com.example.demo.security.UserInfo;
import com.example.demo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    /**
     * 채팅방 생성 또는 기존 채팅방 조회
     */
    @PostMapping("/room")
    public ResponseEntity<ChatRoomResponse> createOrGetChatRoom(
            @RequestBody CreateChatRoomRequestDto request, @AuthenticationPrincipal UserInfo user) {

        Long userId = user.getUser().getUserId();

        ChatRoom chatRoom = chatService.getOrCreateChatRoom(
                userId,
                request.getProviderId(),
                request.getFarmId()
        );

        ChatRoomResponse response = ChatRoomResponse.builder()
                .chatRoomId(chatRoom.getId())
                .message("채팅방이 생성되었습니다.")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자의 채팅방 목록 조회
     */
    @GetMapping("/room/list")
    public ResponseEntity<List<ChatRoomResponseDto>> getChatRoomList(
            @AuthenticationPrincipal UserInfo user) {

        Long userId = user.getUser().getUserId();

        List<ChatRoomResponseDto> chatRooms = chatService.getChatRoomList(userId);
        return ResponseEntity.ok(chatRooms);
    }

    /**
     * 채팅방의 메시지 목록 조회
     */
    @GetMapping("/room/{chatRoomId}")
    public ResponseEntity<Slice<MessageResponse>> getChatMessages(
            @PathVariable Long chatRoomId,
            @PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Slice<ChatMessage> messages = chatService.getChatMessages(chatRoomId, pageable);

        Slice<MessageResponse> messageResponses = messages.map(message -> MessageResponse.builder()
                .chatRoomId(chatRoomId)
                .messageId(message.getId())
                .senderId(message.getSender().getUserId())
                .senderNickname(message.getSender().getNickname())
                .message(message.getMessage())
                .createdAt(message.getCreatedAt())
                .imageUrls(message.getImageUrls())
                .build());

        return ResponseEntity.ok(messageResponses);
    }

    /**
     * 채팅방 farm info
     */
    @GetMapping("/room/{chatRoomId}/farm")
    public ResponseEntity<ChatRoomResponseDto> getChatRoomFarm(@PathVariable Long chatRoomId){
        return ResponseEntity.ok(chatService.getChatRoomFarmInfo(chatRoomId));
    }

    /**
     * 메시지 읽음 처리
     */
    @PatchMapping("/room/{chatRoomId}/read")
    public ResponseEntity<String> markMessagesAsRead(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal UserInfo user) {

        Long userId = user.getUser().getUserId();
        chatService.markMessagesAsRead(chatRoomId, userId);
        return ResponseEntity.ok("메시지를 읽음 처리했습니다.");
    }

    /**
     * 이미지 전송
     */
    @PostMapping("/room/{chatRoomId}/images")
    public ResponseEntity<MessageResponse> sendImageMessage(
            @PathVariable Long chatRoomId,
            @RequestParam("images") List<MultipartFile> imageFiles,
            @AuthenticationPrincipal UserInfo user) throws IOException {

        Long senderId = user.getUser().getUserId();
        ChatMessage savedMessage = chatService.sendImageMessage(chatRoomId, senderId, imageFiles);

        MessageResponse response = MessageResponse.builder()
                .messageId(savedMessage.getId())
                .senderId(savedMessage.getSender().getUserId())
                .senderNickname(savedMessage.getSender().getNickname())
                .message(savedMessage.getMessage())
                .messageType(savedMessage.getMessageType())
                .imageUrls(savedMessage.getImageUrls())
                .createdAt(savedMessage.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    // Request/Response DTOs

    @lombok.Getter
    @lombok.Builder
    public static class ChatRoomResponse {
        private Long chatRoomId;
        private String message;
    }

    @lombok.Getter
    @lombok.Builder
    public static class MessageResponse {
        private Long chatRoomId;
        private Long messageId;
        private Long senderId;
        private String senderNickname;
        private String message;
        private MessageType messageType; // 추가 필요
        private List<String> imageUrls; // 추가 필요
        private java.time.LocalDateTime createdAt;
    }
}
