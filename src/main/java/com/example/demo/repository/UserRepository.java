package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long > {
    Optional<User> findBySocialIdAndSocialType(String socialId, Enum socialType);

    boolean existsBySocialIdAndSocialType(String socialId, Enum socialType);

    Optional<User> findBySocialId(String socialId);
}
