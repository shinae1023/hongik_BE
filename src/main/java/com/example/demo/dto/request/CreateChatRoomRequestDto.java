package com.example.demo.dto.request;

import lombok.*;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRoomRequestDto {
        private Long consumerId;
        private Long providerId;
        private Long farmId;
}
