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
public class FarmCreateResponseDto {
    private Long userId;
    private Long id;
    private String title;
    private String description;
    private String address;
    private Integer price;
    private Integer rentalPeriod;
    private Integer size;
    private Theme theme;
    private String bank;
    private String accountNumber;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}
