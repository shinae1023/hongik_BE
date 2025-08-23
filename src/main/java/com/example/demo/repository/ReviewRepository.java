package com.example.demo.repository;

import com.example.demo.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    //최신순 정렬
    List<Review> findByFarmIdOrderByCreatedAtDesc(Long farmId);

    //등록순 정렬
    List<Review> findByFarmIdOrderByCreatedAtAsc(Long farmId);

}
