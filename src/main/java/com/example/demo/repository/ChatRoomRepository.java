package com.example.demo.repository;

import com.example.demo.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 사용자의 모든 채팅방 목록을 Fetch Join을 사용하여 조회합니다.
     * N+1 문제를 해결하기 위해 연관된 엔티티(consumer, provider, farm)를 함께 가져옵니다.
     * @param userId 사용자의 ID
     * @return 해당 사용자가 참여중인 모든 채팅방 리스트
     */
    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
            "JOIN FETCH cr.consumer c " +
            "JOIN FETCH cr.provider p " +
            "JOIN FETCH cr.farm f " +
            "WHERE c.userId = :userId OR p.userId = :userId")
    List<ChatRoom> findChatRoomsByUserIdWithDetails(@Param("userId") Long userId);

    /**
     * 사용자의 모든 채팅방 목록을 조회합니다.
     * @param userId 사용자의 ID (구매자 또는 판매자)
     * @return 해당 사용자가 참여중인 모든 채팅방 리스트
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.consumer.userId = :userId OR cr.provider.userId = :userId")
    List<ChatRoom> findChatRoomsByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자들이 특정 텃밭에 대해 나눈 채팅방을 조회합니다.
     * @param consumerId 구매자 ID
     * @param providerId 판매자 ID
     * @param farmId 텃밭 ID
     * @return 조건에 맞는 채팅방 (Optional)
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE (cr.consumer.userId = :consumerId AND cr.provider.userId = :providerId AND cr.farm.id = :farmId)")
    Optional<ChatRoom> findChatRoomByUsersAndFarm(@Param("consumerId") Long consumerId, @Param("providerId") Long providerId, @Param("farmId") Long farmId);
}
