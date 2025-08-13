package com.example.demo.repository;

import com.example.demo.entity.ChatMessage;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Slice<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);

    // 이 메소드를 사용하여 N+1 문제 없이 메시지와 이미지를 함께 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId ORDER BY cm.createdAt DESC")
    Slice<ChatMessage> findByChatRoomIdWithImages(@Param("chatRoomId") Long chatRoomId, Pageable pageable);
}
