package com.example.demo.service;

import com.example.demo.dto.request.LikeRequestDto;
import com.example.demo.dto.response.PostSummaryDto;
import com.example.demo.repository.*;
import com.example.demo.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    //좋아요 생성
    public Long createLike(@PathVariable Long postId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        Like like = Like.builder()
                .user(user)
                .post(post)
                .build();

        return likeRepository.save(like).getId();
    }

    //좋아요로 post 조회
    public List<PostSummaryDto> getLikedPosts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        List<Like> likes = likeRepository.findByUser(user);

        return likes.stream()
                .map(Like::getPost)
                .map(post -> {
                    // Set에서 첫 번째 요소를 안전하게 가져오기 위해 스트림을 사용
                    String thumbnailUrl = post.getImages().stream()
                            .findFirst()
                            .map(image -> image.getImageUrl())
                            .orElse(null);

                    return PostSummaryDto.builder()
                            .id(post.getId())
                            .category(post.getCategory())
                            .title(post.getTitle())
                            .authorNickname(post.getUser().getNickname())
                            .likeCount((long) post.getLikes().size())
                            .thumbnailUrl(thumbnailUrl) // 수정된 썸네일 URL을 사용
                            .build();
                })
                .collect(Collectors.toList());
    }
}
