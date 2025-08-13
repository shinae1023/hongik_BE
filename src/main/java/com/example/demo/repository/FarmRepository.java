package com.example.demo.repository;

import com.example.demo.entity.Farm;
import com.example.demo.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long>{
    Optional<Farm> findById(Long id);
    List<Farm> findByUserUserId(Long userId);
    List<Farm> findByUserUserIdAndIsAvailable(Long userId, boolean isAvailable);
    
    List<Farm> findByTitleContainingIgnoreCase(String title);
    
    @Query("SELECT DISTINCT f FROM Farm f WHERE " +
           "(:minPrice IS NULL OR f.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR f.price <= :maxPrice) AND " +
           "(:minSize IS NULL OR f.size >= :minSize) AND " +
           "(:maxSize IS NULL OR f.size <= :maxSize)")
    List<Farm> findFarmsWithBasicFilters(@Param("minPrice") Integer minPrice,
                                        @Param("maxPrice") Integer maxPrice,
                                        @Param("minSize") Integer minSize,
                                        @Param("maxSize") Integer maxSize);
    
    @Query("SELECT DISTINCT f FROM Farm f WHERE " +
           "(:preferredDong IS NULL OR LOWER(f.address) LIKE LOWER(CONCAT('%', :preferredDong, '%'))) OR " +
           "(:preferredThemes IS NULL OR f.theme IN :preferredThemes)")
    List<Farm> findRecommendedFarms(@Param("preferredDong") String preferredDong, 
                                   @Param("preferredThemes") Set<Theme> preferredThemes);
}
