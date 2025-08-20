package com.example.demo.dto.response;

import com.example.demo.entity.Theme;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmDto {
    private Long userId;
    private Long id;
    private String title;
    private String address;
    private Integer price;
    private Integer rentalPeriod;
    private Integer size;
    private String thumbnailUrl;
    private boolean bookmarked;
    private Theme theme;
    private Long borrowerId;
    private boolean ownerAuth;
    private LocalDateTime updateTime;
}
