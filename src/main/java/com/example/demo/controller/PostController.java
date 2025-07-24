package com.example.demo.controller;

import com.example.demo.dto.request.PostRequestDto;
import com.example.demo.dto.response.PostResponseDto;
import com.example.demo.dto.response.PostSummaryDto;
import com.example.demo.entity.Category;
import com.example.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @PostMapping
    public ResponseEntity<Long> createPost(@RequestBody PostRequestDto dto) {
        return ResponseEntity.status(201).body(postService.createPost(dto));
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    // 유저가 작성한 게시글 목록 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostSummaryDto>> getUserPosts(@PathVariable Long userId) {
        return ResponseEntity.ok(postService.getUserPosts(userId));
    }

    //게시글 전체 조회
    @GetMapping("/list")
    public ResponseEntity<List<PostSummaryDto>> getAllPosts() {
        return ResponseEntity.ok(postService.getPostsAll());
    }

    //카테고리별 조회
    @GetMapping("/tip")
    public ResponseEntity<List<PostSummaryDto>> getPostsTip() {
        return ResponseEntity.ok(postService.getPostsTip());
    }

    @GetMapping("/feed")
    public ResponseEntity<List<PostSummaryDto>> getPostsFeed() {
        return ResponseEntity.ok(postService.getPostsFeed());
    }

    @GetMapping("/search/{title}")
    public ResponseEntity<List<PostSummaryDto>> getPostsSearch(@PathVariable String title) {
        return ResponseEntity.ok(postService.getPostsByTitle(title));
    }

}
