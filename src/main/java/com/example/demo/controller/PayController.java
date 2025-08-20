package com.example.demo.controller;

import com.example.demo.dto.request.PayRequestDto; // DTO import
import com.example.demo.security.UserInfo;
import com.example.demo.service.PayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*; // @RestController, @RequestBody import

@RestController // ✅ @Controller -> @RestController 로 변경
@RequiredArgsConstructor
@RequestMapping("/pay")
public class PayController {
    private final PayService payService;

    @PatchMapping("/charge")
    // ✅ @RequestParam -> @RequestBody PayRequestDto 로 변경
    public ResponseEntity<String> chargeCoin(@AuthenticationPrincipal UserInfo user, @RequestBody PayRequestDto requestDto) {
        Long userId = user.getUser().getUserId();
        String resultMessage = payService.chargeCoin(userId, requestDto.getMoney());
        // ✅ Void 대신 처리 결과를 메시지로 반환
        return ResponseEntity.ok(resultMessage);
    }

    @PatchMapping("/exchange")
    // ✅ @RequestParam -> @RequestBody PayRequestDto 로 변경
    public ResponseEntity<String> exchangeCoin(@AuthenticationPrincipal UserInfo user, @RequestBody PayRequestDto requestDto) {
        Long userId = user.getUser().getUserId();
        String resultMessage = payService.exchangeCoin(userId, requestDto.getMoney());
        // ✅ Void 대신 처리 결과를 메시지로 반환
        return ResponseEntity.ok(resultMessage);
    }

    @GetMapping
    public ResponseEntity<Integer> getCoin(@AuthenticationPrincipal UserInfo user) {
        Long userId = user.getUser().getUserId();
        return ResponseEntity.ok(payService.getMyCoin(userId));
    }
}
