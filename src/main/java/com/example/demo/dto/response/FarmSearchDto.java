package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FarmSearchDto {
    private Long id;
    private String title;
    private Integer price;
    private String rentalPeriod;
    private String address;
    private Boolean isAvailable;
}
