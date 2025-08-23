package com.example.demo.dto.response;

import com.example.demo.entity.Theme;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmDetailResponseDto {
    private Long id;
    private String title;
    private String address;
    private Integer size;
    private Integer price;
    private Integer rentalPeriod;
    private Theme theme;
    private String description;
    private List<String> imageUrls;
    private UserDto owner;
    private boolean bookmarked;
    private LocalDateTime createdAt;
    private Long borrowerId;
    private LocalDateTime updatedTime;
    private boolean ownerAuth;
    private boolean isAvailable;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Long userId;
        private String nickname;
        private String profileImage;
    }
}
