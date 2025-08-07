package com.example.demo.service;


import com.example.demo.dto.request.CommentRequestDto;
import com.example.demo.dto.response.CommentResponseDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Long createComment(@PathVariable Long postId, @RequestBody CommentRequestDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .user(user)
                .post(post)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        return commentRepository.save(comment).getId();
    }

    //게시물 세부 조회
    public List<CommentResponseDto> getComment(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        return commentRepository.findByPost(post).stream()
                .map(comment -> CommentResponseDto.builder()
                        .userId(comment.getUser().getUserId())
                        .content(comment.getContent())
                        .postId(comment.getPost().getId())
                        .createdAt(comment.getCreatedAt())
                        .authorNickname(comment.getUser().getNickname())
                        .build()).collect(Collectors.toList());

    }
}
