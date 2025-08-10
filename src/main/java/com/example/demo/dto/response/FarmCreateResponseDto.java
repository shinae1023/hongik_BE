package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FarmCreateResponseDto {
    private Long id;
    private String title;
    private String description;
    private String address;
    private String rentalPeriod;
    private Integer price;
    private LocalDateTime createdAt;
    private Boolean isAvailable;
    private Integer size;
}
