package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String message;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "chat_message_images", joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>(); // 순서 보장을 위해 List 사용

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Builder
    public ChatMessage(ChatRoom chatRoom, User sender, String message, MessageType messageType, List<String> imageUrls) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.message = message;
        this.messageType = messageType != null ? messageType : MessageType.TEXT; // 파라미터 값 사용
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }
}
