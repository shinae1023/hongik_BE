package com.example.demo.service;

import com.example.demo.dto.response.ChatRoomResponseDto;
import com.example.demo.dto.response.PaymentReadyDto;
import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.Farm;
import com.example.demo.entity.User;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.FarmRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final FarmRepository farmRepository;
    private final UserRepository userRepository;

    // 1. 채팅방 조회 or 생성
    public ChatRoomResponseDto enterChatRoom(Long farmId, Long consumerId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 농장입니다."));
        User consumer = userRepository.findById(consumerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        User provider = farm.getUser(); // 텃밭 등록자

        // 이미 존재하면 재사용
        ChatRoom chatRoom = chatRoomRepository.findByConsumerAndFarm(consumer, farm)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.builder()
                        .consumer(consumer)
                        .provider(provider)
                        .farm(farm)
                        .createdAt(LocalDateTime.now())
                        .build()
                ));

        return toDto(chatRoom);
    }

    // 2. 채팅방 상세 조회
    public ChatRoomResponseDto getChatRoom(Long chatroomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatroomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));
        return toDto(chatRoom);
    }

    // 3. 결제 준비 정보 조회
    public PaymentReadyDto getPaymentReadyInfo(Long chatroomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatroomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));
        Farm farm = chatRoom.getFarm();
        User provider = chatRoom.getProvider();

        return PaymentReadyDto.builder()
                .chatroomId(chatRoom.getId())
                .farmTitle(farm.getTitle())
                .price(farm.getPrice())
                .rentalPeriod(farm.getRentalPeriod())
                .providerNickname(provider.getNickname())
                .bank(provider.getBank()) // 유저 Entity에 은행/계좌 필드 필요
                .accountNumber(provider.getAccountNumber())
                .build();
    }

    private ChatRoomResponseDto toDto(ChatRoom chatRoom) {
        return ChatRoomResponseDto.builder()
                .chatroomId(chatRoom.getId())
                .farm(ChatRoomResponseDto.FarmInfo.builder()
                        .id(chatRoom.getFarm().getId())
                        .title(chatRoom.getFarm().getTitle())
                        .price(chatRoom.getFarm().getPrice())
                        .build())
                .consumer(ChatRoomResponseDto.UserInfo.builder()
                        .id(chatRoom.getConsumer().getUserId())
                        .nickname(chatRoom.getConsumer().getNickname())
                        .profileImage(chatRoom.getConsumer().getProfileImage())
                        .build())
                .provider(ChatRoomResponseDto.UserInfo.builder()
                        .id(chatRoom.getProvider().getUserId())
                        .nickname(chatRoom.getProvider().getNickname())
                        .profileImage(chatRoom.getProvider().getProfileImage())
                        .build())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}

