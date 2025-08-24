package com.example.demo.dto.response;

import com.example.demo.dto.request.PayFarmRequestDto;
import com.example.demo.entity.FarmImage;
import com.example.demo.entity.Theme;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ChatRoomResponseDto {
    private Long chatroomId;
    private FarmInfo farm; // 이 부분을 PayFarmRequestDto로 사용
    private UserInfo provider;
    private UserInfo consumer;
    private LocalDateTime createdAt;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private int unreadCount;

    @Getter
    @Builder
    public static class FarmInfo {
        private Long id;
        private String title;
        private String address;
        private Integer size;
        private Integer price;
        private Integer rentalPeriod;
        private Theme theme;
        private String description;
        private String thumbnailUrl;
        private List<String> imageUrls; // 썸네일 대신 전체 이미지 URL 리스트
        private OwnerInfo owner; // 소유자 정보 추가
        private LocalDateTime createdAt; // 생성 시간 추가
        private LocalDateTime updatedTime; // 수정 시간 추가
        private boolean isAvailable; // 이용 가능 여부 추가
    }

    @Getter
    @Builder
    public static class OwnerInfo { // PayFarmRequestDto.UserDto와 동일한 구조
        private Long userId;
        private String nickname;
        private String profileImage;
    }

    @Getter
    @Builder
    public static class UserInfo {
        private Long id;
        private String nickname;
        private String profileImage;
    }
}

