package com.example.demo.repository;

import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.Farm;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByConsumerAndFarm(User consumer, Farm farm);
}
