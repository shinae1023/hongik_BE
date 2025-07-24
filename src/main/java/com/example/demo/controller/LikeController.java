package com.example.demo.controller;

import com.example.demo.dto.request.LikeRequestDto;
import com.example.demo.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/like")
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/{postId}")
    public ResponseEntity<Long> createLike(@PathVariable Long postId, @RequestBody LikeRequestDto dto) {
        return ResponseEntity.status(201).body(likeService.createLike(postId,dto));
    }
}
