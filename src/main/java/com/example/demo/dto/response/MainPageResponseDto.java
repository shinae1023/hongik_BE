package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainPageResponseDto {
    private String message;
    private List<FarmDto> farms;
    private List<FarmDto> recommendedFarms;
}