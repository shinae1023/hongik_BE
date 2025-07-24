package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
public class CommentResponseDto {
    private Long userId;
    private Long postId;
    private String content;
    private Timestamp createdAt;
}
