package com.example.demo.service;

import com.example.demo.dto.response.FarmDto;
import com.example.demo.dto.response.FarmListResponseDto;
import com.example.demo.dto.response.UserResponseDto;
import com.example.demo.entity.Farm;
import com.example.demo.entity.User;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.BookmarkRepository;
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
    private final BookmarkRepository bookmarkRepository;

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
        boolean bookmarked = false;
        if (currentUserId != null) {
            bookmarked = bookmarkRepository.existsByUserUserIdAndFarmId(currentUserId, farm.getId());
        }


        return FarmDto.builder()
                .id(farm.getId())
                .title(farm.getTitle())
                .address(farm.getAddress())
                .price(farm.getPrice())
                .rentalPeriod(farm.getRentalPeriod())
                .size(farm.getSize())
                .thumbnailUrl(farm.getImages().isEmpty() ? null : farm.getImages().get(0).getImageUrl())
                .bookmarked(bookmarked)
                .theme(farm.getTheme())
                .build();
    }
}

