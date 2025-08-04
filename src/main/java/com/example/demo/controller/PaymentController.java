package com.example.demo.controller;

import com.example.demo.dto.response.PaymentReadyDto;
import com.example.demo.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {
    private final ChatRoomService chatRoomService;

    // 결제 준비 정보 조회
    @GetMapping("/ready/{chatroomId}")
    public ResponseEntity<PaymentReadyDto> getPaymentReady(@PathVariable Long chatroomId) {
        return ResponseEntity.ok(chatRoomService.getPaymentReadyInfo(chatroomId));
    }
}
