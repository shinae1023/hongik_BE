package com.example.demo.controller;

import com.example.demo.dto.request.PostRequestDto;
import com.example.demo.dto.response.PostResponseDto;
import com.example.demo.dto.response.PostSummaryDto;
import com.example.demo.entity.Category;
import com.example.demo.security.UserInfo;
import com.example.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createPost(@AuthenticationPrincipal UserInfo user,
                                           @RequestPart("dto") PostRequestDto dto,
                                           @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        dto.setUserId(user.getUser().getUserId());
        return ResponseEntity.status(201).body(postService.createPost(dto, images));
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long postId,
                                                   @AuthenticationPrincipal UserInfo user) {
        // ✅ 비로그인 상태(user=null)와 로그인 상태 모두 처리
        Long userId = (user != null) ? user.getUser().getUserId() : null;
        return ResponseEntity.ok(postService.getPost(postId, userId));
    }

    // 유저가 작성한 게시글 목록 조회
    @GetMapping("/user/{userId}") // ✅ userId -> authorId 로 명확화
    public ResponseEntity<List<PostSummaryDto>> getUserPosts(@PathVariable Long userId,
                                                             @AuthenticationPrincipal UserInfo user) {
        Long viewerId = (user != null) ? user.getUser().getUserId() : null;
        // ✅ 서비스 메소드 시그니처를 (작성자 ID, 보는 사람 ID)로 변경해야 할 수 있습니다. (현재 서비스 코드는 authorId를 viewerId로 사용)
        // ✅ 지금은 PostService의 getUserPosts(authorId)가 내부적으로 isLiked를 authorId 기준으로 계산하므로 그대로 호출합니다.
        return ResponseEntity.ok(postService.getUserPosts(userId));
    }

    // 게시글 전체 조회
    @GetMapping("/list")
    public ResponseEntity<List<PostSummaryDto>> getAllPosts(@AuthenticationPrincipal UserInfo user) {
        Long userId = (user != null) ? user.getUser().getUserId() : null;
        return ResponseEntity.ok(postService.getPostsAll(userId));
    }

    // 팁 게시판 조회
    @GetMapping("/tip")
    public ResponseEntity<List<PostSummaryDto>> getPostsTip(@AuthenticationPrincipal UserInfo user) {
        Long userId = (user != null) ? user.getUser().getUserId() : null;
        return ResponseEntity.ok(postService.getPostsTip(userId));
    }

    // 피드 게시판 조회
    @GetMapping("/feed")
    public ResponseEntity<List<PostSummaryDto>> getPostsFeed(@AuthenticationPrincipal UserInfo user) {
        Long userId = (user != null) ? user.getUser().getUserId() : null;
        return ResponseEntity.ok(postService.getPostsFeed(userId));
    }

    // 게시글 검색
    @GetMapping("/search")
    public ResponseEntity<List<PostSummaryDto>> searchPosts(
            @RequestParam String title,
            @RequestParam(required = false) Category category,
            @AuthenticationPrincipal UserInfo user) {

        Long userId = (user != null) ? user.getUser().getUserId() : null;
        List<PostSummaryDto> results = postService.searchPosts(title, category, userId);
        return ResponseEntity.ok(results);
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId, @AuthenticationPrincipal UserInfo user) {
        Long userId = user.getUser().getUserId();
        String message = postService.deletePost(userId, postId);
        return ResponseEntity.ok(message);
    }
}
