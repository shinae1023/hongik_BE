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
    private Integer price;
    private List<String> images;
    private Boolean isAvailable;
    private LocalDateTime createdAt;

}
