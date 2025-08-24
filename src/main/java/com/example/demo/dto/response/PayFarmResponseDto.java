package com.example.demo.dto.response;
import com.example.demo.entity.Theme;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class PayFarmResponseDto {
    private FarmDto farm;
    private String EcoScore;
    private String coin;
    private String bank;
    private String accountNumber;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Long userId;
        private String nickname;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FarmDto{
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
        private boolean isAvailable;
    }

}
