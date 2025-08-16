package com.example.demo.service;

import com.example.demo.dto.request.ReviewRequest;
import com.example.demo.dto.response.ReviewResponse;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse createReview(Long farmId, ReviewRequest request, Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. (ID: " + userId + ")"));

        Review review = Review.builder()
                .farmId(farmId)
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .content(request.getContent())
                .build();

        user.updateEcoScore(10);
        Review savedReview = reviewRepository.save(review);

        return ReviewResponse.builder()
                .reviewId(savedReview.getId())
                .userId(savedReview.getUserId())
                .nickname(savedReview.getNickname())
                .farmId(savedReview.getFarmId())
                .content(savedReview.getContent())
                .createdAt(savedReview.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByFarmId(Long farmId, String sortBy) {
        List<Review> reviews;
        if ("createdAt_asc".equalsIgnoreCase(sortBy)) {
            reviews = reviewRepository.findByFarmIdOrderByCreatedAtAsc(farmId);
        } else {
            reviews = reviewRepository.findByFarmIdOrderByCreatedAtDesc(farmId);
        }

        return reviews.stream()
                .map(review -> ReviewResponse.builder()
                        .reviewId(review.getId())
                        .userId(review.getUserId())
                        .nickname(review.getNickname())
                        .farmId(review.getFarmId())
                        .content(review.getContent())
                        .createdAt(review.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("해당 리뷰를 찾을 수 없습니다."));

        if (!review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("해당 리뷰를 삭제할 권한이 없습니다.");
        }

        reviewRepository.delete(review);
    }
}
