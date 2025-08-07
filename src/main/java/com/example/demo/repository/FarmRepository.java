package com.example.demo.repository;

import com.example.demo.dto.response.FarmListDto;
import com.example.demo.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FarmRepository extends JpaRepository<Farm, Long> {
    List<Farm> findByTitleContaining(String title);
    List<Farm> findByUserId(Long userId);
    List<Farm> findByUserIdAndIsAvailable(Long userId, boolean isAvailable);
}
