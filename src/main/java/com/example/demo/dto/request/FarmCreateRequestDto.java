package com.example.demo.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmCreateRequestDto {
    private String title;
    private String description;
    private String address;
    private Integer price;
    private Integer rentalPeriod;
    private Integer size;
    private String theme;
    private List<MultipartFile> images;
    private String bank;
    private String accountNumber;
}
