package com.example.demo.repository;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByUser(User user);
    List<Comment> findByPost(Post post);
    List<Comment> findByPostIdOrderByCreatedAtDesc(Post postId);

    Optional<Comment> findByUser_UserIdAndPostId(Long userId, Long postId);
}
