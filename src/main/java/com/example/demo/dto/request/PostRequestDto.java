package com.example.demo.dto.request;

import com.example.demo.entity.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostRequestDto {
    private String title;
    private String content;
    private Long userId;
    private Category category;
}

