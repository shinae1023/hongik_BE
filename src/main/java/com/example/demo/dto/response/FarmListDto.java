package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FarmListDto {
    private Long id;
    private String title;
    private String price;
    private String rentalPeriod;
    private String address;
    private String thumbnailUrl;
    private Boolean isAvailable;

}
