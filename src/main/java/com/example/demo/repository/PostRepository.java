package com.example.demo.repository;

import com.example.demo.dto.response.PostSummaryDto;
import com.example.demo.entity.Category;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUser(User user);
    List<Post> findByCategory(Category category);
    List<Post> findByTitleContaining(String title);
    // N+1 문제 해결을 위해 Fetch Join 사용
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.likes")
    List<Post> findAllWithLikes();
}
