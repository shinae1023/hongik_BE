package com.example.demo.controller;

import com.example.demo.dto.request.ReviewRequest;
import com.example.demo.dto.response.ReviewResponse;
import com.example.demo.service.ReviewService;
import com.example.demo.security.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/{farmId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByFarmId(
            @PathVariable Long farmId,
            @RequestParam(required = false, defaultValue = "createdAt_desc") String sortBy) {
        return ResponseEntity.ok(reviewService.getReviewsByFarmId(farmId, sortBy));
    }

    @PostMapping("/{farmId}")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long farmId,
            @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserInfo userInfo) {
        ReviewResponse response = reviewService.createReview(farmId, request, userInfo.getUser().getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserInfo userInfo) {
        reviewService.deleteReview(reviewId, userInfo.getUser().getUserId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
