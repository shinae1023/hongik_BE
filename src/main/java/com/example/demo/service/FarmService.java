package com.example.demo.service;

import com.example.demo.dto.request.FarmCreateRequestDto;
import com.example.demo.dto.response.FarmCreateResponseDto;
import com.example.demo.dto.response.FarmDetailResponseDto;
import com.example.demo.dto.response.FarmDto;
import com.example.demo.dto.response.FarmListResponseDto;
import com.example.demo.entity.Bookmark;
import com.example.demo.entity.Farm;
import com.example.demo.entity.FarmImage;
import com.example.demo.entity.User;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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


    public FarmCreateResponseDto createFarm(
            FarmCreateRequestDto requestDto,
            List<MultipartFile> images,
            Long userId) throws IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. (ID: " + userId + ")"));

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

        Farm savedFarm = farmRepository.save(farm);

        List<String> uploadedImageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            List<FarmImage> farmImages = new ArrayList<>();
            for (MultipartFile imageFile : images) {
                String imageUrl = s3Uploader.upload(imageFile, "farm-images");
                uploadedImageUrls.add(imageUrl);

                FarmImage farmImage = FarmImage.of(imageUrl, savedFarm);
                farmImages.add(farmImage);
            }
            farmImages.forEach(savedFarm::addImage);
            farmImageRepository.saveAll(farmImages);
        }

        return FarmCreateResponseDto.builder()
                .id(savedFarm.getId())
                .title(savedFarm.getTitle())
                .description(savedFarm.getDescription())
                .address(savedFarm.getAddress())
                .rentalPeriod(savedFarm.getRentalPeriod())
                .price(savedFarm.getPrice())
                .size(savedFarm.getSize())
                .theme(savedFarm.getTheme())
                .imageUrls(uploadedImageUrls)
                .bank(requestDto.getBank())
                .accountNumber(requestDto.getAccountNumber())
                .createdAt(savedFarm.getCreatedAt())
                .build();
    }

    public FarmListResponseDto getAllFarms(Long userId) {
        List<Farm> farms = farmRepository.findAll();

        List<FarmDto> farmDtos = farms.stream()
                .map(farm -> toFarmDto(farm, userId))
                .collect(Collectors.toList());

        return FarmListResponseDto.builder()
                .message("모든 텃밭 매물 목록입니다.")
                .farms(farmDtos)
                .build();
    }

    public FarmDetailResponseDto getFarmDetail(String farmId, Long userId) {
        Farm farm = farmRepository.findById(Long.parseLong(farmId))
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 텃밭입니다. (ID: " + farmId + ")"));

        boolean isBookmarked = false;
        if (userId != null) {
            isBookmarked = bookmarkRepository.existsByUserUserIdAndFarmId(userId, farm.getId());
        }

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
                        .id(farm.getUser().getUserId())
                        .nickname(farm.getUser().getNickname())
                        .build())
                .isBookmarked(isBookmarked)
                .createdAt(farm.getCreatedAt())
                .build();
    }


    private FarmDto toFarmDto(Farm farm, Long currentUserId) {
        boolean isBookmarked = false;
        if (currentUserId != null) {
            isBookmarked = bookmarkRepository.existsByUserUserIdAndFarmId(currentUserId, farm.getId());
        }

        return FarmDto.builder()
                .id(farm.getId())
                .title(farm.getTitle())
                .price(farm.getPrice())
                .rentalPeriod(farm.getRentalPeriod())
                .address(farm.getAddress())
                .size(farm.getSize())
                .thumbnailUrl(farm.getImages().isEmpty() ? null : farm.getImages().get(0).getImageUrl())
                .isBookmarked(isBookmarked)
                .build();
    }

    @Transactional
    public void addBookmark(String farmId, Long userId) {
        User user = userRepository.findById(userId)
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. (ID: " + userId + ")"));
        Farm farm = farmRepository.findById(Long.parseLong(farmId))
                .orElseThrow(() -> new EntityNotFoundException("텃밭을 찾을 수 없습니다. (ID: " + farmId + ")"));

        Bookmark bookmark = bookmarkRepository.findByUserAndFarm(user, farm)
                .orElseThrow(() -> new EntityNotFoundException("해당 텃밭은 북마크되어 있지 않습니다."));

        bookmarkRepository.delete(bookmark);
    }
}
