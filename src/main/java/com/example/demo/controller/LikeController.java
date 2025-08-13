package com.example.demo.controller;

import com.example.demo.dto.request.LikeRequestDto;
import com.example.demo.security.UserInfo;
import com.example.demo.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/like")
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/{postId}")
    public ResponseEntity<Long> createLike(@PathVariable Long postId, @AuthenticationPrincipal UserInfo user) {
        Long userId = user.getUser().getUserId();
        return ResponseEntity.status(201).body(likeService.createLike(postId,userId));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteLike(@PathVariable Long postId, @AuthenticationPrincipal UserInfo user) {
        Long userId = user.getUser().getUserId();
        likeService.deleteLike(postId,userId);
        return ResponseEntity.noContent().build();
    }
}
