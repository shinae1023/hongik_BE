package com.example.demo.repository;

import com.example.demo.entity.Bookmark;
import com.example.demo.entity.Farm;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Book;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserAndFarm(User user, Farm farm);

    boolean existsByUserUserIdAndFarmId(Long userId, Long farmId);

    Optional<Bookmark> findByUserAndFarm(User user, Farm farm);

    List<Bookmark> findByUser(User user);
}
