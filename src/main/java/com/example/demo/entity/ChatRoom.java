package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    private LocalDateTime createdAt;

    @Builder
    public ChatRoom(User consumer, User provider, Farm farm, LocalDateTime createdAt) {
        this.consumer = consumer;
        this.provider = provider;
        this.farm = farm;
        this.createdAt = createdAt;
    }
}
