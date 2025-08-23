package com.example.demo.dto.request;
import com.example.demo.dto.response.FarmDetailResponseDto;
import com.example.demo.entity.Theme;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayFarmRequestDto {
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
    private LocalDateTime updatedTime;
    private boolean isAvailable;
    private Integer ecoScoreUse;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Long userId;
        private String nickname;

    }

    public static class PayFarmDto{

    }
}
