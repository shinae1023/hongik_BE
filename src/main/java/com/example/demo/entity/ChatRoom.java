package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User consumer; // 구매자

    @ManyToOne(fetch = FetchType.LAZY)
    private User provider; // 판매자

    @ManyToOne(fetch = FetchType.LAZY)
    private Farm farm; // 채팅 대상 농장

    private String lastMessage; // 마지막 메시지 내용
    private LocalDateTime lastMessageAt; // 마지막 메시지 시간

    private int consumerUnreadCount; // 구매자 안읽은 메시지 수
    private int providerUnreadCount; // 판매자 안읽은 메시지 수

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();


    @Builder
    public ChatRoom(User consumer, User provider, Farm farm) {
        this.consumer = consumer;
        this.provider = provider;
        this.farm = farm;
        this.consumerUnreadCount = 0;
        this.providerUnreadCount = 0;
    }

    // 편의 메서드 (새로운 메시지 발생 시 호출)
    public void updateOnNewMessage(String message, LocalDateTime sentAt, User sender) {
        this.lastMessage = message;
        this.lastMessageAt = sentAt;
        // 메시지 보낸 사람이 판매자이면, 구매자의 안읽은 카운트 증가
        if (this.provider.getUserId().equals(sender.getUserId())) {
            this.consumerUnreadCount++;
        } else {
            this.providerUnreadCount++;
        }
    }

    public void resetConsumerUnreadCount() {
        this.consumerUnreadCount = 0;
    }

    public void resetProviderUnreadCount() {
        this.providerUnreadCount = 0;
    }
}
