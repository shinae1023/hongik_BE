package com.example.demo.service;

import com.example.demo.dto.response.FarmDetailDto;
import com.example.demo.dto.response.FarmListDto;
import com.example.demo.dto.response.FarmSearchDto;
import com.example.demo.entity.Farm;
import com.example.demo.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FarmService {
    private final FarmRepository farmRepository;

    // ✅ 전체 목록 조회
    public List<FarmListDto> getAllFarms() {
        return farmRepository.findAll().stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }

    // ✅ 상세 조회
    public FarmDetailDto getFarmDetail(Long farmId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 텃밭입니다."));

        return FarmDetailDto.builder()
                .id(farm.getId())
                .title(farm.getTitle())
                .description(farm.getDescription())
                .address(farm.getAddress())
                .rentalPeriod(farm.getRentalPeriod())
                .price(formatPrice(farm.getPrice()))
                .isAvailable(farm.getIsAvailable())
                .images(farm.getImages().stream()
                        .map(image -> image.getImageUrl())
                        .collect(Collectors.toList()))
                .providerProfile(FarmDetailDto.ProviderProfileDto.builder()
                        .nickname(farm.getProvider().getNickname())
                        .email(farm.getProvider().getEmail())
                        .phone(farm.getProvider().getPhone())
                        .build())
                .accountInfo(FarmDetailDto.AccountInfoDto.builder()
                        .bank(farm.getAccountInfo().getBank())
                        .accountNumber(farm.getAccountInfo().getAccountNumber())
                        .build())
                .createdAt(farm.getCreatedAt())
                .build();
    }

    // ✅ 제목 검색 - FarmSearchDto 반환
    public List<FarmSearchDto> searchByTitle(String title) {
        return farmRepository.findByTitleContaining(title).stream()
                .map(this::toSearchDto)
                .collect(Collectors.toList());
    }

    // 🔧 리스트 DTO 매핑
    private FarmListDto toListDto(Farm farm) {
        String thumbnailUrl = farm.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                .findFirst()
                .map(img -> img.getImageUrl())
                .orElse(null);

        return FarmListDto.builder()
                .id(farm.getId())
                .title(farm.getTitle())
                .price(formatPrice(farm.getPrice()))
                .rentalPeriod(farm.getRentalPeriod())
                .address(farm.getAddress())
                .thumbnailUrl(thumbnailUrl)
                .isAvailable(farm.getIsAvailable())
                .build();
    }

    // 🔧 검색 결과 DTO 매핑
    private FarmSearchDto toSearchDto(Farm farm) {
        String thumbnailUrl = farm.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                .findFirst()
                .map(img -> img.getImageUrl())
                .orElse(null);

        return FarmSearchDto.builder()
                .id(farm.getId())
                .title(farm.getTitle())
                .address(farm.getAddress())
                .thumbnailUrl(thumbnailUrl)
                .isAvailable(farm.getIsAvailable())
                .build();
    }

    // 💰 숫자 가격 → 포맷팅 (ex: "월 30,000원")
    private String formatPrice(Integer price) {
        return String.format("월 %,d원", price);
    }
}
