package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class FarmDetailDto {
    private Long id;
    private String title;
    private String description;
    private String address;
    private String rentalPeriod;
    private String price;
    private List<String> images;
    private ProviderProfileDto providerProfile;
    private AccountInfoDto accountInfo;
    private Boolean isAvailable;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class ProviderProfileDto {
        private String nickname;
        private String email;
        private String phone;
    }

    @Getter
    @Builder
    public static class AccountInfoDto {
        private String bank;
        private String accountNumber;
    }
}
