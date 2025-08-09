package com.example.demo.repository;

import com.example.demo.dto.response.FarmListDto;
import com.example.demo.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FarmRepository extends JpaRepository<Farm, Long> {
    List<Farm> findByTitleContaining(String title);
    List<Farm> findByUserUserId(Long userId);
    List<Farm> findByUserUserIdAndIsAvailable(Long userId, boolean isAvailable);
}
