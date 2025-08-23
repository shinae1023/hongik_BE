package com.example.demo.controller;

import com.example.demo.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotProxyController {
    private final ChatbotService chatbotService;

    @PostMapping("/chat")
    public Mono<ResponseEntity<Map<String, String>>> chat(@RequestBody Map<String, String> req) {
        String query = req.get("query");
        return chatbotService.ask(query)
                .map(resp -> ResponseEntity.ok(Map.of("response", resp)))
                .onErrorResume(e -> {
                    // 로깅 추가 가능
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", e.getMessage())));
                });
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
