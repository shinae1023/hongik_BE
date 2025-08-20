package com.example.demo.service;

import com.example.demo.dto.request.UserUpdateRequestDto;
import com.example.demo.dto.response.FarmDto;
import com.example.demo.dto.response.FarmListResponseDto;
import com.example.demo.dto.response.UserResponseDto;
import com.example.demo.entity.Farm;
import com.example.demo.entity.User;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.FarmRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MypageService {
    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final S3Uploader s3Uploader;

    //마이페이지 사용자 정보
    @Transactional(readOnly = true)
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
                .phoneNumber(user.getPhone())
                .build();
    }

    @Transactional(readOnly = true)
    public EcoScoreResopnseDto getEcoScore(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException("사용자를 찾을 수 없습니다"));
        return EcoScoreResopnseDto.builder()
                .userId(user.getUserId())
                .ecoscore(user.getEcoScore())
                .build();
    }

    //내가 등록한 모든 매물 조회
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
                .message("내 텃밭 조회")
                .farms(farmDtos)
                .build();
    }

    //내가 대여중인 텃밭
    @Transactional(readOnly = true)
    public FarmListResponseDto getFarmsUsed(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));

        List<Farm> farms = farmRepository.findByBorrowerId(userId);

        List<FarmDto> farmDtos = farms.stream()
                .map(farm -> toFarmDto(farm, userId))
                .collect(Collectors.toList());

        return FarmListResponseDto.builder()
                .message("빌린 텃밭 목록 조회")
                .farms(farmDtos)
                .build();
    }

    private FarmDto toFarmDto(Farm farm, Long currentUserId) {
        boolean bookmarked = false;
        // 여기에 BookmarkRepository를 사용하여 북마크 여부 확인 로직 추가
        // 예: bookmarked = bookmarkRepository.existsByFarmIdAndOwnerId(farm.getId(), currentUserId);
        // 북마크 기능이 아직 구현되지 않았다면 false로 고정

        return FarmDto.builder()
                .userId(farm.getUser().getUserId())
                .id(farm.getId())
                .title(farm.getTitle())
                .address(farm.getAddress())
                .price(farm.getPrice())
                .rentalPeriod(farm.getRentalPeriod())
                .size(farm.getSize())
                .thumbnailUrl(farm.getImages().isEmpty() ? null : farm.getImages().get(0).getImageUrl())
                .bookmarked(bookmarked)
                .theme(farm.getTheme())
                .borrowerId(farm.getBorrowerId())
                .build();
    }

    //유저 정보 수정
    @Transactional
    public UserResponseDto updateUser(Long userId, UserUpdateRequestDto requestDto) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        user.updateMypageInfo(requestDto);

        // 3. 수정된 정보를 DTO로 변환하여 반환
        return UserResponseDto.builder()
                .nickname(user.getNickname())
                .imageUrl(user.getProfileImage())
                .address(user.getAddress())
                .ecoScore(user.getEcoScore())
                .bank(user.getBank())
                .accountNumber(user.getAccountNumber())
                .phoneNumber(user.getPhone())
                .addressDong(user.getPreferredDong())
                .preferredThemes(user.getPreferredThemes())
                .build();
    }

    //프로필 사진 등록
    @Transactional
    public String updateUserProfileImage(Long userId, MultipartFile profileImage)  {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        try {
            // 기존 프로필 이미지가 있는 경우 S3에서 삭제
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                s3Uploader.deleteFile(user.getProfileImage());
            }

            // 새 프로필 이미지를 S3에 업로드
            String imageUrl = s3Uploader.upload(profileImage, "profile"); // "profile"은 S3 버킷 내의 폴더명

            // 사용자 정보에 새 이미지 URL 저장
            user.updateProfileImage(imageUrl);

            return "프로필 사진 변경 완료 : " + imageUrl; // 새 이미지 URL 반환

        } catch (IOException e) {
            // 파일 업로드 실패 시 예외 처리
            throw new RuntimeException("프로필 이미지 업로드에 실패했습니다.", e);
        }
    }

    @Builder
    @Getter
    public static class EcoScoreResopnseDto {
        private Long userId;
        private int ecoscore;
    }

}
