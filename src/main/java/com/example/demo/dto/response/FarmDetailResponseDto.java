package com.example.demo.dto.response;

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
    private String theme;
    private String description;
    private List<String> imageUrls;
    private UserDto owner;
    private boolean isBookmarked;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Long id;
        private String nickname;
    }
}
