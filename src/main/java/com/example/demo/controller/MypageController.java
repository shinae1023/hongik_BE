package com.example.demo.controller;

import com.example.demo.dto.request.UserUpdateRequestDto;
import com.example.demo.dto.response.FarmListResponseDto;
import com.example.demo.dto.response.PostSummaryDto;
import com.example.demo.dto.response.UserResponseDto;
import com.example.demo.entity.User;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.security.UserInfo;
import com.example.demo.service.LikeService;
import com.example.demo.service.MypageService;
import com.example.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {
    private final MypageService mypageService;
    private final PostService postService;
    private final LikeService likeService;

    //마이페이지 회원 정보 조회
    @GetMapping("/profile")
    public ResponseEntity<UserResponseDto> getUsers(@AuthenticationPrincipal UserInfo user) {
        Long userId = user.getUser().getUserId();
        return ResponseEntity.ok(mypageService.getUsers(userId));
    }

    @GetMapping("/ecoscore")
    public ResponseEntity<MypageService.EcoScoreResopnseDto> getEcoScore(@AuthenticationPrincipal UserInfo user) {
        Long userId = user.getUser().getUserId();
        return ResponseEntity.ok(mypageService.getEcoScore(userId));
    }

    //등록한 모든 텃밭 조회
    @GetMapping("/farm")
    public ResponseEntity<FarmListResponseDto> getFarms(@AuthenticationPrincipal UserInfo user) {
        Long userId = user.getUser().getUserId();
        return ResponseEntity.ok(mypageService.getMyFarms(userId));
    }

    //등록한 매물 중 대여중인 텃밭
    @GetMapping("/farm/used")
    public ResponseEntity<FarmListResponseDto> getFarmsUsed(@AuthenticationPrincipal UserInfo user) {
        Long userId = user.getUser().getUserId();
        return ResponseEntity.ok(mypageService.getFarmsUsed(userId));
    }

    // 유저가 작성한 게시글 목록 조회
    @GetMapping("/post")
    public ResponseEntity<List<PostSummaryDto>> getUserPosts(@AuthenticationPrincipal UserInfo user) {
        Long userId = user.getUser().getUserId();
        return ResponseEntity.ok(postService.getUserPosts(userId));
    }

    //유저가 좋아요한 게시글 목록 조회
    @GetMapping("/post/like")
    public ResponseEntity<List<PostSummaryDto>> getUserPostsLiked(@AuthenticationPrincipal UserInfo user) {
        Long userId = user.getUser().getUserId();
        return ResponseEntity.ok(likeService.getLikedPosts(userId));
    }

    @PatchMapping("/edit")
    public ResponseEntity<UserResponseDto> userUpdate(@AuthenticationPrincipal UserInfo user, @RequestBody UserUpdateRequestDto requestDto) {
        Long userId = user.getUser().getUserId();
        return ResponseEntity.ok(mypageService.updateUser(userId,requestDto));
    }

    @PostMapping("/edit/image")
    public ResponseEntity<String> updateUserProfileImage(@AuthenticationPrincipal UserInfo user, @RequestPart("image") MultipartFile imagefile){
        Long userId = user.getUser().getUserId();
        return ResponseEntity.ok(mypageService.updateUserProfileImage(userId,imagefile));
    }
}
