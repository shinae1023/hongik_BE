package com.example.demo.dto.request;

import com.example.demo.entity.Theme;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmCreateRequestDto {
    private Long userId;
    private String title;
    private String description;
    private String address;
    private Integer price;
    private Integer rentalPeriod;
    private Integer size;
    private Theme theme;
}
