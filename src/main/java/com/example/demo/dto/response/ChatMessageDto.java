package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ChatMessageDto {
    private Long senderId;
    private String message;
    private LocalDateTime sentAt;

    @Builder
    public ChatMessageDto(Long senderId, String message, LocalDateTime sentAt) {
        this.senderId = senderId;
        this.message = message;
        this.sentAt = sentAt;
    }
}
