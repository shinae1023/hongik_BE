package com.example.demo.controller;

import com.example.demo.dto.request.FarmCreateRequestDto;
import com.example.demo.dto.response.FarmCreateResponseDto;
import com.example.demo.dto.response.FarmDetailResponseDto;
import com.example.demo.dto.response.FarmListResponseDto;
import com.example.demo.dto.response.FarmSearchResponseDto;
import com.example.demo.service.FarmService;
import com.example.demo.security.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/farm")
public class FarmController {
    private final FarmService farmService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FarmCreateResponseDto> createFarm(
            @RequestPart("dto") FarmCreateRequestDto requestDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserInfo user) throws IOException {

        Long userId = user.getUser().getUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(farmService.createFarm(requestDto, images, userId));
    }

    @GetMapping
    public ResponseEntity<FarmSearchResponseDto> searchFarms(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer minSize,
            @RequestParam(required = false) Integer maxSize,
            @RequestParam(required = false) String theme,
            @AuthenticationPrincipal UserInfo user) {

        Long userId = (user != null && user.getUser() != null) ? user.getUser().getUserId() : null;

        if (title != null && !title.trim().isEmpty()) {
            return ResponseEntity.ok(farmService.searchFarmsByTitle(title, userId));
        } else {
            return ResponseEntity.ok(farmService.searchFarmsWithFilters(location, minPrice, maxPrice, minSize, maxSize, theme, userId));
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<FarmListResponseDto> getAllFarms(
            @AuthenticationPrincipal UserInfo user) {

        Long userId = (user != null && user.getUser() != null) ? user.getUser().getUserId() : null;

        return ResponseEntity.ok(farmService.getAllFarms(userId));
    }
    
    @GetMapping("/recommended")
    public ResponseEntity<FarmSearchResponseDto> getRecommendations(
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @AuthenticationPrincipal UserInfo user) {

        if (user == null || user.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = user.getUser().getUserId();
        return ResponseEntity.ok(farmService.getRecommendedFarms(userId, limit));
    }

    @GetMapping("/{farmId}")
    public ResponseEntity<FarmDetailResponseDto> getFarmDetail(
                                                                @PathVariable String farmId,
                                                                @AuthenticationPrincipal UserInfo user) {

        Long userId = (user != null && user.getUser() != null) ? user.getUser().getUserId() : null;

        return ResponseEntity.ok(farmService.getFarmDetail(farmId, userId));
    }

    @PostMapping("/{farmId}/bookmark")
    public ResponseEntity<Void> bookmarkFarm(
            @PathVariable String farmId,
            @AuthenticationPrincipal UserInfo user) {

        Long userId = user.getUser().getUserId();
        farmService.addBookmark(farmId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{farmId}/bookmark")
    public ResponseEntity<Void> unbookmarkFarm(
            @PathVariable String farmId,
            @AuthenticationPrincipal UserInfo user) {

        Long userId = user.getUser().getUserId();
        farmService.removeBookmark(farmId, userId);
        return ResponseEntity.noContent().build();
    }
}
