package com.example.demo.dto.response;

import com.example.demo.entity.Category;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Builder
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private Long viewCount;
    private Category category;
    private Timestamp createdAt;
    private Timestamp modifiedAt;
    private String authorNickname;
    private List<String> imageUrls;
    private List<CommentResponseDto> comments;
    private Long likeCount;
}

