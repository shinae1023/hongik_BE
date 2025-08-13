package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmDto {
    private Long id;
    private String title;
    private String address;
    private Integer price;
    private Integer rentalPeriod;
    private Integer size;
    private String thumbnailUrl;
    private boolean isBookmarked;
    private String theme;
}
