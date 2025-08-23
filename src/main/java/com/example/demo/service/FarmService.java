package com.example.demo.service;

import com.example.demo.dto.request.FarmCreateRequestDto;
import com.example.demo.dto.response.FarmDetailResponseDto;
import com.example.demo.dto.response.FarmDto;
import com.example.demo.dto.response.FarmSearchResponseDto;
import com.example.demo.dto.response.MainPageResponseDto;
import com.example.demo.entity.Bookmark;
import com.example.demo.entity.Farm;
import com.example.demo.entity.FarmImage;
import com.example.demo.entity.User;
import com.example.demo.entity.Theme;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.BookmarkRepository;
import com.example.demo.repository.FarmImageRepository;
import com.example.demo.repository.FarmRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FarmService {
    private final FarmRepository farmRepository;
    private final FarmImageRepository farmImageRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;
    private final BookmarkRepository bookmarkRepository;

    @Transactional
    public Long createFarm(
            FarmCreateRequestDto requestDto,
            List<MultipartFile> images) throws IOException {

        // 1. DTO에서 userId를 가져와 사용자(User)를 찾습니다.
        User user = userRepository.findByUserId(requestDto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. (ID: " + requestDto.getUserId() + ")"));

        // 2. DTO와 User 객체를 사용하여 새로운 Farm 엔티티를 생성합니다.
        Farm farm = Farm.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .address(requestDto.getAddress())
                .rentalPeriod(requestDto.getRentalPeriod())
                .price(requestDto.getPrice())
                .size(requestDto.getSize())
                .theme(requestDto.getTheme())
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        // 3. Farm 엔티티를 저장하고, 사용자(User)의 EcoScore를 업데이트합니다.
        Farm savedFarm = farmRepository.save(farm);
        user.updateEcoScore(50);

        // 4. 이미지가 있다면 S3에 업로드하고, FarmImage 엔티티를 생성 및 저장합니다.
        if (images != null && !images.isEmpty()) {
            List<FarmImage> farmImages = new ArrayList<>();
            for (MultipartFile imageFile : images) {
                String imageUrl = s3Uploader.upload(imageFile, "farm-images");

                FarmImage farmImage = FarmImage.of(imageUrl, savedFarm);
                farmImages.add(farmImage);
            }
            farmImages.forEach(savedFarm::addImage);
            farmImageRepository.saveAll(farmImages);
        }

        // 5. 생성된 Farm의 ID를 반환합니다.
        return savedFarm.getId();
    }

    public MainPageResponseDto getMainPageFarms(Long userId) {
        List<Farm> allFarms = farmRepository.findAllByOrderByUpdateTimeDesc();
        List<FarmDto> farmDtos = allFarms.stream()
                .map(farm -> toFarmDto(farm, userId))
                .collect(Collectors.toList());

        if (userId == null) {
            return MainPageResponseDto.builder()
                    .message("모든 텃밭 매물 목록입니다.")
                    .farms(farmDtos)
                    .recommendedFarms(new ArrayList<>())
                    .build();
        } else {
            FarmSearchResponseDto recommendedResponse = getRecommendedFarms(userId);
            List<FarmDto> recommendedFarms = recommendedResponse.getFarms();

            return MainPageResponseDto.builder()
                    .message("메인 페이지 정보입니다.")
                    .farms(farmDtos)
                    .recommendedFarms(recommendedFarms)
                    .build();
        }
    }

    public FarmDetailResponseDto getFarmDetail(String farmId, Long userId) {
        Farm farm = farmRepository.findById(Long.parseLong(farmId))
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 텃밭입니다. (ID: " + farmId + ")"));

        boolean bookmarked = false;
        if (userId != null) {
            bookmarked = bookmarkRepository.existsByUserUserIdAndFarmId(userId, farm.getId());
        }

        boolean isOwner = false;
        if(Objects.equals(userId, farm.getUser().getUserId())) {isOwner = true;}

        return FarmDetailResponseDto.builder()
                .id(farm.getId())
                .title(farm.getTitle())
                .description(farm.getDescription())
                .address(farm.getAddress())
                .rentalPeriod(farm.getRentalPeriod())
                .price(farm.getPrice())
                .size(farm.getSize())
                .imageUrls(farm.getImages().stream()
                        .map(FarmImage::getImageUrl)
                        .collect(Collectors.toList()))
                .owner(FarmDetailResponseDto.UserDto.builder()
                        .userId(farm.getUser().getUserId())
                        .nickname(farm.getUser().getNickname())
                        .build())
                .bookmarked(bookmarked)
                .createdAt(farm.getCreatedAt())
                .theme(farm.getTheme())
                .borrowerId(farm.getBorrowerId())
                .updatedTime(farm.getUpdateTime())
                .ownerAuth(isOwner) //isOwner = true 인 경우, 매물 수정 및 프리미엄 매물 등록 버튼 있어야 함
                .isAvailable(farm.isAvailable())
                .build();
    }


    private FarmDto toFarmDto(Farm farm, Long currentUserId) {
        boolean bookmarked = false;
        if (currentUserId != null) {
            bookmarked = bookmarkRepository.existsByUserUserIdAndFarmId(currentUserId, farm.getId());
        }

        return FarmDto.builder()
                .userId(farm.getUser().getUserId())
                .id(farm.getId())
                .title(farm.getTitle())
                .price(farm.getPrice())
                .rentalPeriod(farm.getRentalPeriod())
                .address(farm.getAddress())
                .size(farm.getSize())
                .thumbnailUrl(farm.getImages().isEmpty() ? null : farm.getImages().get(0).getImageUrl())
                .bookmarked(bookmarked)
                .theme(farm.getTheme())
                .borrowerId(farm.getBorrowerId())
                .createdAt(farm.getCreatedAt())
                .updateTime(farm.getUpdateTime())
                .isAvailable(farm.isAvailable())
                .build();
    }

    @Transactional
    public void addBookmark(String farmId, Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. (ID: " + userId + ")"));
        Farm farm = farmRepository.findById(Long.parseLong(farmId))
                .orElseThrow(() -> new EntityNotFoundException("텃밭을 찾을 수 없습니다. (ID: " + farmId + ")"));

        if (bookmarkRepository.existsByUserAndFarm(user, farm)) {
            throw new IllegalArgumentException("이미 북마크된 텃밭입니다.");
        }

        Bookmark bookmark = Bookmark.createBookmark(user, farm);
        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void removeBookmark(String farmId, Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. (ID: " + userId + ")"));
        Farm farm = farmRepository.findById(Long.parseLong(farmId))
                .orElseThrow(() -> new EntityNotFoundException("텃밭을 찾을 수 없습니다. (ID: " + farmId + ")"));

        Bookmark bookmark = bookmarkRepository.findByUserAndFarm(user, farm)
                .orElseThrow(() -> new EntityNotFoundException("해당 텃밭은 북마크되어 있지 않습니다."));

        bookmarkRepository.delete(bookmark);
    }

    public FarmSearchResponseDto searchFarmsByTitle(String title, Long userId) {
        List<Farm> farms = farmRepository.findByTitleContainingIgnoreCaseOrderByUpdateTimeDesc(title.trim());
        String message = "'" + title + "'으로 검색한 결과입니다.";

        if (farms.isEmpty()) {
            message = "'" + title + "'으로 검색한 결과가 없습니다.";
        }

        List<FarmDto> farmDtos = farms.stream()
                .map(farm -> toFarmDto(farm, userId))
                .collect(Collectors.toList());

        return FarmSearchResponseDto.builder()
                .message(message)
                .farms(farmDtos)
                .build();
    }

    public FarmSearchResponseDto searchFarmsWithFilters(String location, Integer minPrice, Integer maxPrice,
                                                       Integer minSize, Integer maxSize, String theme, Long userId) {

        List<Farm> farms = farmRepository.findFarmsWithBasicFilters(minPrice, maxPrice, minSize, maxSize);

        if (location != null && !location.trim().isEmpty()) {
            List<String> locations = parseCommaSeparatedValues(location);
            farms = farms.stream()
                    .filter(farm -> locations.stream()
                            .anyMatch(loc -> farm.getAddress().toLowerCase().contains(loc.toLowerCase())))
                    .collect(Collectors.toList());
        }

        if (theme != null && !theme.trim().isEmpty()) {
            List<String> themes = parseCommaSeparatedValues(theme);
            farms = farms.stream()
                    .filter(farm -> themes.stream()
                            .anyMatch(t -> farm.getTheme() != null && farm.getTheme().name().toLowerCase().equals(t.toLowerCase())))
                    .collect(Collectors.toList());
        }

        String message;
        if (farms.isEmpty()) {
            message = "필터링 조건에 맞는 매물이 없습니다.";
        } else {
            message = "필터링된 텃밭 목록입니다.";
        }

        List<FarmDto> farmDtos = farms.stream()
                .map(farm -> toFarmDto(farm, userId))
                .collect(Collectors.toList());

        return FarmSearchResponseDto.builder()
                .message(message)
                .farms(farmDtos)
                .build();
    }

    public FarmSearchResponseDto getRecommendedFarms(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. (ID: " + userId + ")"));

        String preferredDong = (user.getPreferredDong() != null && !user.getPreferredDong().trim().isEmpty()) 
                ? user.getPreferredDong().trim() : null;
        
        Set<Theme> preferredThemes = (user.getPreferredThemes() != null && !user.getPreferredThemes().isEmpty()) 
                ? user.getPreferredThemes() : null;

        List<Farm> recommendedFarms = farmRepository.findRecommendedFarms(preferredDong, preferredThemes);

        if (recommendedFarms.isEmpty()) {
            recommendedFarms = farmRepository.findAll();
        }

        List<FarmDto> farmDtos = recommendedFarms.stream()
                .map(farm -> toFarmDto(farm, userId))
                .collect(Collectors.toList());

        return FarmSearchResponseDto.builder()
                .farms(farmDtos)
                .build();
    }

    private List<String> parseCommaSeparatedValues(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    //프리미엄 매물 등록
    @Transactional
    public int FarmPremium(Long farmId, Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException("존재하지 않는 유저입니다"));

        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 텃밭입니다."));

        if (!farm.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("프리미엄 등록 권한이 없습니다."); // 혹은 다른 권한 관련 예외
        }

        farm.checkAndResetPremiumCount();

        if (farm.getPremiumCount() >= 5) {
            throw new IllegalStateException("하루 프리미엄 등록 횟수(5회)를 모두 사용했습니다.");
        }

        farm.increasePremiumCount();
        user.updateEcoScore(-100);
        farm.updateTime();

        return farm.getPremiumCount();
    }
}
