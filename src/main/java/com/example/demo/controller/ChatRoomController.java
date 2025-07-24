package com.example.demo.controller;

import com.example.demo.dto.response.ChatRoomResponseDto;
import com.example.demo.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatroom")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    @GetMapping("/{farmId}")
    public ResponseEntity<ChatRoomResponseDto> getChatRoom(@PathVariable Long farmId) {
        return ResponseEntity.ok(chatRoomService.getChatRoom(farmId));
    }
}
