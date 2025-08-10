package com.example.demo.repository;

import com.example.demo.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FarmRepository extends JpaRepository<Farm, Long>{
    Optional<Farm> findById(Long id);
    List<Farm> findByUserUserId(Long userId);
    List<Farm> findByUserUserIdAndIsAvailable(Long userId, boolean isAvailable);
}
