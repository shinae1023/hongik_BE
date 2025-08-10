package com.example.demo.dto.request;

import lombok.Getter;

@Getter
public class FarmCreateRequestDto {
    private String title;
    private String description;
    private String address;
    private String rentalPeriod;
    private Integer price;
    private Integer size;
}
