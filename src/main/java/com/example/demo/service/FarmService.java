package com.example.demo.service;

import com.example.demo.dto.request.FarmCreateRequestDto;
import com.example.demo.dto.response.FarmCreateResponseDto;
import com.example.demo.dto.response.FarmDetailDto;
import com.example.demo.dto.response.FarmListDto;
import com.example.demo.dto.response.FarmSearchDto;
import com.example.demo.entity.Farm;
import com.example.demo.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FarmService {
    private final FarmRepository farmRepository;

    //텃밭 매물 생성
    public FarmCreateResponseDto createFarm(FarmCreateRequestDto request) {
        Farm farm = Farm.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .address(request.getAddress())
                .rentalPeriod(request.getRentalPeriod())
                .price(request.getPrice())
                .createdAt(LocalDateTime.now())
                .isAvailable(true)
                .size(request.getSize())
                .build();

        Farm saved = farmRepository.save(farm);

        return FarmCreateResponseDto.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .address(saved.getAddress())
                .rentalPeriod(saved.getRentalPeriod())
                .price(saved.getPrice())
                .createdAt(saved.getCreatedAt())
                .isAvailable(saved.getIsAvailable())
                .size(saved.getSize())
                .build();
    }

    // 전체 목록 조회
    public List<FarmListDto> getAllFarms() {
        return farmRepository.findAll().stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }

    // 상세 조회
    public FarmDetailDto getFarmDetail(Long farmId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 텃밭입니다."));

        return FarmDetailDto.builder()
                .id(farm.getId())
                .title(farm.getTitle())
                .description(farm.getDescription())
                .address(farm.getAddress())
                .rentalPeriod(farm.getRentalPeriod())
                .price(farm.getPrice())
                .isAvailable(farm.getIsAvailable())
                .createdAt(farm.getCreatedAt())
                .size(farm.getSize())
                .build();
    }

    // 검색
    public List<FarmSearchDto> searchByTitle(String title) {
        return farmRepository.findByTitleContaining(title).stream()
                .map(this::toSearchDto)
                .collect(Collectors.toList());
    }

    private FarmListDto toListDto(Farm farm) {
        return FarmListDto.builder()
                .id(farm.getId())
                .title(farm.getTitle())
                .price(farm.getPrice())
                .rentalPeriod(farm.getRentalPeriod())
                .address(farm.getAddress())
                .isAvailable(farm.getIsAvailable())
                .size(farm.getSize())
                .build();
    }

    private FarmSearchDto toSearchDto(Farm farm) {
        return FarmSearchDto.builder()
                .id(farm.getId())
                .title(farm.getTitle())
                .address(farm.getAddress())
                .isAvailable(farm.getIsAvailable())
                .size(farm.getSize())
                .build();
    }

}
