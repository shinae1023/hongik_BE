package com.example.demo.dto.response;

import com.example.demo.entity.FarmImage;
import com.example.demo.entity.Theme;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Getter
@Builder
public class ChatRoomResponseDto {
    private Long chatroomId;

    private FarmInfo farm;
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
        private String thumbnailUrl;
        private String description;
        private Integer price;
        private Integer rentalPeriod;
        private Integer size;
        private String address;
        private Theme theme;
    }

    @Getter
    @Builder
    public static class UserInfo {
        private Long id;
        private String nickname;
        private String profileImage;
    }
}

