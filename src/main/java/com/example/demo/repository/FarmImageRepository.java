package com.example.demo.repository;

import com.example.demo.entity.FarmImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmImageRepository extends JpaRepository<FarmImage, Long> {
    List<FarmImage> findByFarmId(Long farmId);
}
