package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomResponseDto {
    private Long chatroomId;

    private FarmInfo farm;
    private UserInfo provider;
    private UserInfo consumer;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class FarmInfo {
        private Long id;
        private String title;
        private String thumbnailUrl;
        private Integer price;
    }

    @Getter
    @Builder
    public static class UserInfo {
        private Long id;
        private String nickname;
        private String profileImage;
    }
}
