package com.example.demo.controller;

import com.example.demo.dto.request.PayFarmRequestDto;
import com.example.demo.dto.request.PayRequestDto; // DTO import
import com.example.demo.dto.response.ExchangeResponseDto;
import com.example.demo.dto.response.PayFarmResponseDto;
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

    @PatchMapping
    public ResponseEntity<PayFarmResponseDto> pay(@AuthenticationPrincipal UserInfo user, @RequestBody PayFarmRequestDto dto) {
        Long userId = user.getUser().getUserId();
        return ResponseEntity.ok(payService.pay(dto,userId));
    }

    @GetMapping("/bank")
    public ResponseEntity<String> getBank(@AuthenticationPrincipal UserInfo user){
        Long userId = user.getUser().getUserId();
        return ResponseEntity.ok(payService.getMyBank(userId));
    }

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
    public ResponseEntity<ExchangeResponseDto> exchangeCoin(@AuthenticationPrincipal UserInfo user, @RequestBody PayRequestDto requestDto) {
        Long userId = user.getUser().getUserId();
        // ✅ Void 대신 처리 결과를 메시지로 반환
        return ResponseEntity.ok(payService.exchangeCoin(userId, requestDto.getMoney()));
    }

    @GetMapping
    public ResponseEntity<Integer> getCoin(@AuthenticationPrincipal UserInfo user) {
        Long userId = user.getUser().getUserId();
        return ResponseEntity.ok(payService.getMyCoin(userId));
    }
}
