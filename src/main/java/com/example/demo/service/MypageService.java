package com.example.demo.service;

import com.example.demo.dto.response.FarmListDto;
import com.example.demo.dto.response.UserResponseDto;
import com.example.demo.entity.Farm;
import com.example.demo.entity.User;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.FarmRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MypageService {
    private final UserRepository userRepository;
    private final FarmRepository farmRepository;

    //마이페이지 사용자 정보
    public UserResponseDto getUsers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));

        return UserResponseDto.builder()
                .nickname(user.getNickname())
                .imageUrl(user.getProfileImage())
                .address(user.getAddress())
                .ecoScore(user.getEcoScore())
                .bank(user.getBank())
                .accountNumber(user.getAccountNumber())
                .build();
    }

    //내가 등록한 모든 매물 조회
    public List<FarmListDto> getMyFarms(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));

        List<Farm> farms = farmRepository.findByUserUserId(userId);

        // 3. 조회된 Farm 엔티티 목록을 FarmListDto 목록으로 변환하여 반환
        return farms.stream()
                .map(farm -> FarmListDto.builder()
                        .id(farm.getId())
                        .title(farm.getTitle())
                        .address(farm.getAddress())
                        .price(farm.getPrice())
                        .isAvailable(farm.getIsAvailable())
                        .build())
                .collect(Collectors.toList());
    }

    //등록한 매물 중 대여중인 텃밭
    public List<FarmListDto> getFarmsUsed(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));

        List<Farm> farms = farmRepository.findByUserUserIdAndIsAvailable(userId, false);

        return farms.stream()
                .map(farm -> FarmListDto.builder()
                        .id(farm.getId())
                        .title(farm.getTitle())
                        .address(farm.getAddress())
                        .price(farm.getPrice())
                        .isAvailable(farm.getIsAvailable())
                        .build())
                .collect(Collectors.toList());
    }
}
