package com.example.demo.controller;

import com.example.demo.service.ChatbotService;
import com.example.demo.service.DiseaseDiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotProxyController {
    private final ChatbotService chatbotService;
    private final DiseaseDiagnosisService diagnosisService;

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

    @PostMapping(value = "/diagnose", consumes = "multipart/form-data")
    public Mono<ResponseEntity<Map<String, String>>> diagnose(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam("plantType") String plantType) {

        return diagnosisService.diagnose(imageFile, plantType)
                .map(response -> ResponseEntity.ok(Map.of("response", response)))
                .onErrorResume(e -> {
                    // 🟢 e.getMessage()가 null일 경우를 대비하여 기본 에러 메시지를 설정합니다.
                    String errorMessage = Optional.ofNullable(e.getMessage())
                            .orElse("AI 서버와 통신 중 알 수 없는 오류가 발생했습니다.");
                    return Mono.just(ResponseEntity.status(500).body(Map.of("error", errorMessage)));
                });
    }
}
