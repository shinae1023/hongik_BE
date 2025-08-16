package com.example.demo.controller;

import com.example.demo.dto.request.LikeRequestDto;
import com.example.demo.security.UserInfo;
import com.example.demo.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/like")
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/{postId}")
    public ResponseEntity<?> createLike(@PathVariable Long postId, @AuthenticationPrincipal UserInfo user) {
        try {
            Long userId = user.getUser().getUserId();
            Long likeId = likeService.createLike(postId, userId);

            // 성공 시: 생성된 리소스의 ID와 함께 201 Created 응답
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("likeId", likeId));

        } catch (IllegalStateException e) {
            // '이미 좋아요를 누른 게시물입니다.' 예외가 발생했을 때

            // 실패 시: 에러 메시지와 함께 409 Conflict 응답
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) //
                    .body(Map.of("message", e.getMessage())); // 서비스의 에러 메시지를 그대로 전달
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteLike(@PathVariable Long postId, @AuthenticationPrincipal UserInfo user) {
        Long userId = user.getUser().getUserId();
        likeService.deleteLike(postId,userId);
        return ResponseEntity.noContent().build();
    }
}
