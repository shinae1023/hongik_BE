package com.example.demo.dto.response;

import com.example.demo.entity.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostSummaryDto {
    private Long id;
    private Category category;
    private String title;
    private String authorNickname;
    private String thumbnailUrl;
}

