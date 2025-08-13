package com.example.demo.controller;

import com.example.demo.dto.response.FarmListResponseDto;
import com.example.demo.dto.response.PostSummaryDto;
import com.example.demo.dto.response.UserResponseDto;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.UserInfo;
import com.example.demo.service.LikeService;
import com.example.demo.service.MypageService;
import com.example.demo.service.PostService;
import com.example.demo.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile/{userId}")
//다른 유저의 프로필 조회
public class ProfileController {
    private final ProfileService profileService;
    private final PostService postService;
    private final LikeService likeService;

    @GetMapping
    public ResponseEntity<UserResponseDto> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getUsers(userId));
    }

    //등록한 모든 텃밭 조회
    @GetMapping("/farm")
    public ResponseEntity<FarmListResponseDto> getFarms(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getMyFarms(userId));
    }

    //등록한 매물 중 대여중인 텃밭
    @GetMapping("/farm/used")
    public ResponseEntity<FarmListResponseDto> getFarmsUsed(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getFarmsUsed(userId));
    }

    // 유저가 작성한 게시글 목록 조회
    @GetMapping("/post")
    public ResponseEntity<List<PostSummaryDto>> getUserPosts(@PathVariable Long userId) {
        return ResponseEntity.ok(postService.getUserPosts(userId));
    }

    //유저가 좋아요한 게시글 목록 조회
    @GetMapping("/post/like")
    public ResponseEntity<List<PostSummaryDto>> getUserPostsLiked(@PathVariable Long userId) {
        return ResponseEntity.ok(likeService.getLikedPosts(userId));
    }
}
