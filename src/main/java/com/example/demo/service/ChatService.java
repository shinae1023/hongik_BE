package com.example.demo.service;

import com.example.demo.controller.WebSocketChatController;
import com.example.demo.dto.response.ChatMessageDto;
import com.example.demo.dto.response.ChatRoomResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.FarmRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final S3Uploader s3Uploader;
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * 채팅방 생성 또는 기존 채팅방 반환
     */
    @Transactional
    public ChatRoom getOrCreateChatRoom(Long consumerId, Long providerId, Long farmId) {
        User consumer = userRepository.findById(consumerId)
                .orElseThrow(() -> new IllegalArgumentException("구매자를 찾을 수 없습니다."));
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다."));
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new IllegalArgumentException("텃밭을 찾을 수 없습니다."));

        // 기존 채팅방 확인
        return chatRoomRepository.findChatRoomByUsersAndFarm(consumerId, providerId, farmId)
                .orElseGet(() -> {
                    ChatRoom newChatRoom = ChatRoom.builder()
                            .consumer(consumer)
                            .provider(provider)
                            .farm(farm)
                            .build();
                    return chatRoomRepository.save(newChatRoom);
                });
    }

    /**
     * 사용자의 채팅방 목록 조회
     */
    public List<ChatRoomResponseDto> getChatRoomList(Long userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsByUserIdWithDetails(userId);

        return chatRooms.stream()
                .map(chatRoom -> convertToChatRoomResponseDto(chatRoom, userId))
                .collect(Collectors.toList());
    }

    /**
     * 메시지 전송
     */
    @Transactional
    public ChatMessage sendMessage(Long chatRoomId, Long senderId, String messageContent) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("발신자를 찾을 수 없습니다."));

        // 메시지 생성
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .messageType(MessageType.TEXT)
                .message(messageContent)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 채팅방 정보 업데이트
        chatRoom.updateOnNewMessage(messageContent, savedMessage.getCreatedAt(), sender);
        chatRoomRepository.save(chatRoom);

        return savedMessage;
    }

    /**
    이미지 전송
    */
    @Transactional
    public ChatMessage sendImageMessage(Long chatRoomId, Long senderId, List<MultipartFile> imageFiles) throws IOException {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어있습니다.");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("발신자를 찾을 수 없습니다."));

        // 1. S3에 이미지 업로드 후 URL 목록 받아오기
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile imageFile : imageFiles) {
            String imageUrl = s3Uploader.upload(imageFile, "images");
            imageUrls.add(imageUrl);
        }

        // 2. 이미지 메시지 생성
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .messageType(MessageType.IMAGE) // 메시지 타입을 IMAGE로 설정
                .message("사진") // 마지막 메시지 표시용 텍스트
                .imageUrls(imageUrls) // 이미지 URL 리스트 저장
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 3. 채팅방 정보 업데이트 (마지막 메시지를 "사진"으로)
        chatRoom.updateOnNewMessage(savedMessage.getMessage(), savedMessage.getCreatedAt(), sender);
        chatRoomRepository.save(chatRoom);

        // 4. WebSocket으로 이미지 메시지 브로드캐스팅
        WebSocketChatController.ChatMessageResponse response = WebSocketChatController.ChatMessageResponse.from(savedMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId, response);

        return savedMessage;
    }

    /**
     * 채팅방의 메시지 목록 조회
     */
    public Slice<ChatMessage> getChatMessages(Long chatRoomId, Pageable pageable) {
        if (!chatRoomRepository.existsById(chatRoomId)) {
            throw new IllegalArgumentException("채팅방을 찾을 수 없습니다.");
        }
        return chatMessageRepository.findByChatRoomIdWithImages(chatRoomId, pageable);
    }

    /**
     * 안읽은 메시지 수 초기화
     */
    @Transactional
    public void markMessagesAsRead(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // 구매자가 읽음 처리
        if (chatRoom.getConsumer().getUserId().equals(userId)) {
            chatRoom.resetConsumerUnreadCount();
        }
        // 판매자가 읽음 처리
        else if (chatRoom.getProvider().getUserId().equals(userId)) {
            chatRoom.resetProviderUnreadCount();
        }

        chatRoomRepository.save(chatRoom);
    }

    /**
     * ChatRoom을 ChatRoomResponseDto로 변환
     */
    private ChatRoomResponseDto convertToChatRoomResponseDto(ChatRoom chatRoom, Long currentUserId) {
        // 현재 사용자의 안읽은 메시지 수 계산
        int unreadCount = 0;
        if (chatRoom.getConsumer().getUserId().equals(currentUserId)) {
            unreadCount = chatRoom.getConsumerUnreadCount();
        } else if (chatRoom.getProvider().getUserId().equals(currentUserId)) {
            unreadCount = chatRoom.getProviderUnreadCount();
        }

        String thumbnailUrl = chatRoom.getFarm().getImages().stream()
                .findFirst()
                .map(FarmImage::getImageUrl)
                .orElse(null);

        return ChatRoomResponseDto.builder()
                .chatroomId(chatRoom.getId())
                .farm(ChatRoomResponseDto.FarmInfo.builder()
                        .id(chatRoom.getFarm().getId())
                        .title(chatRoom.getFarm().getTitle())
                        .price(chatRoom.getFarm().getPrice())
                        .thumbnailUrl(thumbnailUrl)
                        .build())
                .provider(ChatRoomResponseDto.UserInfo.builder()
                        .id(chatRoom.getProvider().getUserId())
                        .nickname(chatRoom.getProvider().getNickname())
                        .profileImage(chatRoom.getProvider().getProfileImage())
                        .build())
                .consumer(ChatRoomResponseDto.UserInfo.builder()
                        .id(chatRoom.getConsumer().getUserId())
                        .nickname(chatRoom.getConsumer().getNickname())
                        .profileImage(chatRoom.getConsumer().getProfileImage())
                        .build())
                .createdAt(chatRoom.getCreatedAt())
                .lastMessage(chatRoom.getLastMessage())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .unreadCount(unreadCount)
                .build();
    }
}
