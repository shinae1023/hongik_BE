package com.example.demo.dto.request;

import com.example.demo.entity.Theme;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
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
