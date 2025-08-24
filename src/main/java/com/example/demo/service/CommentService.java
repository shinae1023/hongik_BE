package com.example.demo.service;


import com.example.demo.dto.request.CommentRequestDto;
import com.example.demo.dto.response.CommentResponseDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Like;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
                        .id(comment.getId())
                        .userId(comment.getUser().getUserId())
                        .content(comment.getContent())
                        .postId(comment.getPost().getId())
                        .createdAt(comment.getCreatedAt())
                        .authorNickname(comment.getUser().getNickname())
                        .profileImage(comment.getUser().getProfileImage())
                        .build()).collect(Collectors.toList());

    }

    //댓글 최신순
    public List<CommentResponseDto> getCommentNew(Long postId){
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId).stream()
                .map(comment -> CommentResponseDto.builder()
                        .id(comment.getId())
                        .userId(comment.getUser().getUserId())
                        .content(comment.getContent())
                        .postId(comment.getPost().getId())
                        .createdAt(comment.getCreatedAt())
                        .authorNickname(comment.getUser().getNickname())
                        .profileImage(comment.getUser().getProfileImage())
                        .build()).collect(Collectors.toList());

    }

    //댓글 삭제
    public String deleteComment(Long commentId, Long userId){
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow (()->new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        if(!(comment.getUser().getUserId().equals(userId))){
            throw new SecurityException("댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);
        return "댓글이 삭제되었습니다.";
    }

}
