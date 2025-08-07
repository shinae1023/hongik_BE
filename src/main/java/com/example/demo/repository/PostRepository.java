package com.example.demo.repository;

import com.example.demo.dto.response.PostSummaryDto;
import com.example.demo.entity.Category;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUser(User user);
    List<Post> findByCategory(Category category);
    List<Post> findByTitleContaining(String title);
    // N+1 문제 해결을 위해 Fetch Join 사용
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.likes")
    List<Post> findAllWithLikes();

    List<Post> findByTitleContainingAndCategory(String title, Category category);

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.user " +      // 작성자 정보
            "LEFT JOIN FETCH p.images " +    // 이미지 목록
            "LEFT JOIN FETCH p.comments c " +// 댓글 목록
            "LEFT JOIN FETCH c.user " +      // 댓글의 작성자 정보
            "LEFT JOIN FETCH p.likes " +     // 좋아요 목록
            "WHERE p.id = :postId")
    Optional<Post> findByIdWithDetails(@Param("postId") Long postId);
}
