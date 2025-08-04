// ImageRepository.java
package com.example.demo.repository;

import com.example.demo.entity.Image;
import com.example.demo.entity.Post; // findByPost 메서드를 위해 import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    // 특정 게시물(Post)에 속한 모든 이미지를 찾는 메서드 (선택 사항)
    List<Image> findByPost(Post post);
}
