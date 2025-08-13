package com.example.demo.controller;

import com.example.demo.dto.request.CommentRequestDto;
import com.example.demo.dto.response.CommentResponseDto;
import com.example.demo.security.UserInfo;
import com.example.demo.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;

    //댓글 작성
    @PostMapping("/{postId}")
    public ResponseEntity<Long> createComment(@AuthenticationPrincipal UserInfo user, @PathVariable Long postId, @RequestBody CommentRequestDto dto) {
        dto.setUserId(user.getUser().getUserId());
        return ResponseEntity.status(201).body(commentService.createComment(postId,dto));
    }

    //댓글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentResponseDto>> getComment(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getComment(postId));
    }
}
