package com.example.demo.service;

import com.example.demo.dto.response.FarmDto;
import com.example.demo.dto.response.FarmListResponseDto;
import com.example.demo.dto.response.UserResponseDto;
import com.example.demo.entity.Farm;
import com.example.demo.entity.User;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.FarmRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final FarmRepository farmRepository;

    @Transactional(readOnly = true)
    public UserResponseDto getUsers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));

        return UserResponseDto.builder()
                .nickname(user.getNickname())
                .imageUrl(user.getProfileImage())
                .address(user.getAddress())
                .ecoScore(user.getEcoScore())
                .build();
    }

    @Transactional(readOnly = true)
    public FarmListResponseDto getMyFarms(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));

        List<Farm> farms = farmRepository.findByUserUserId(userId);

        List<FarmDto> farmDtos = farms.stream()
                .map(farm -> toFarmDto(farm, userId))
                .collect(Collectors.toList());

        // 3. 조회된 Farm 엔티티 목록을 FarmListDto 목록으로 변환하여 반환
        return FarmListResponseDto.builder()
                .farms(farmDtos)
                .build();
    }

    //등록한 매물 중 대여중인 텃밭
    @Transactional(readOnly = true)
    public FarmListResponseDto getFarmsUsed(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));

        List<Farm> farms = farmRepository.findByUserUserIdAndIsAvailable(userId, false);

        List<FarmDto> farmDtos = farms.stream()
                .map(farm -> toFarmDto(farm, userId))
                .collect(Collectors.toList());

        return FarmListResponseDto.builder()
                .farms(farmDtos)
                .build();
    }

    private FarmDto toFarmDto(Farm farm, Long currentUserId) {
        boolean isBookmarked = false;
        // 여기에 BookmarkRepository를 사용하여 북마크 여부 확인 로직 추가
        // 예: isBookmarked = bookmarkRepository.existsByFarmIdAndOwnerId(farm.getId(), currentUserId);
        // 북마크 기능이 아직 구현되지 않았다면 false로 고정

        return FarmDto.builder()
                .id(farm.getId())
                .title(farm.getTitle())
                .address(farm.getAddress())
                .price(farm.getPrice())
                .rentalPeriod(farm.getRentalPeriod())
                .size(farm.getSize())
                .thumbnailUrl(farm.getImages().isEmpty() ? null : farm.getImages().get(0).getImageUrl())
                .isBookmarked(isBookmarked)
                .theme(farm.getTheme())
                .build();
    }
}

