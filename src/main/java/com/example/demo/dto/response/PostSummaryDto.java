package com.example.demo.dto.response;

import com.example.demo.entity.Category;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;


@Getter
@Builder
public class PostSummaryDto {
    private Long id;
    private Category category;
    private String title;
    private String authorNickname;
    private String content;
    private String thumbnailUrl;
    private Long likeCount;
    private Timestamp createdAt;
    private boolean isLiked;
}

