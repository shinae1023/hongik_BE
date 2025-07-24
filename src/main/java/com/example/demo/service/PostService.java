package com.example.demo.service;

import com.example.demo.dto.request.PostRequestDto;
import com.example.demo.dto.response.PostResponseDto;
import com.example.demo.dto.response.PostSummaryDto;
import com.example.demo.entity.Category;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Long createPost(PostRequestDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .viewCount(0L)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .modifiedAt(new Timestamp(System.currentTimeMillis()))
                .user(user)
                .build();

        return postRepository.save(post).getId();
    }

    //게시물 세부 조회
    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        return PostResponseDto.builder()
                .id(post.getId())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .authorNickname(post.getUser().getNickname())
                .build();
    }

    //유저별 게시물 조회
    public List<PostSummaryDto> getUserPosts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        return postRepository.findByUser(user).stream()
                .map(post -> PostSummaryDto.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .category(post.getCategory())
                        .authorNickname(post.getUser().getNickname())
                        .build())
                .collect(Collectors.toList());
    }

    //게시물 전체 조회
    public List<PostSummaryDto> getPostsAll() {
        return postRepository.findAll().stream()
                .map(post -> PostSummaryDto.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .category(post.getCategory())
                        .authorNickname(post.getUser().getNickname())
                        .build())
                .collect(Collectors.toList());
    }

    //피드 게시판 조회
    public List<PostSummaryDto> getPostsFeed(){
        return postRepository.findByCategory(Category.FEED).stream()
                .map(post -> PostSummaryDto.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .category(post.getCategory())
                        .authorNickname(post.getUser().getNickname())
                        .build())
                .collect(Collectors.toList());
    }

    //팁 게시판 조회
    public List<PostSummaryDto> getPostsTip(){
        return postRepository.findByCategory(Category.TIP).stream()
                .map(post -> PostSummaryDto.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .category(post.getCategory())
                        .authorNickname(post.getUser().getNickname())
                        .build())
                .collect(Collectors.toList());
    }

    //게시글 검색
    public List<PostSummaryDto> getPostsByTitle(String title) {
        return postRepository.findByTitleContaining(title).stream()
                .map(post -> PostSummaryDto.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .category(post.getCategory())
                        .authorNickname(post.getUser().getNickname())
                        .build())
                .collect(Collectors.toList());
    }

}

