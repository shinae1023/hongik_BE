package com.example.demo.service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final WebClient chatbotWebClient;

    @Value("${chatbot.service-key}")
    private String serviceKey;

    public Mono<String> ask(String query) {
        Map<String, String> body = Map.of("query", query);

        return chatbotWebClient.post()
                .uri("/chat")
                .header("X-Service-Key", serviceKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(m -> (String) m.get("response"))
                .timeout(Duration.ofSeconds(15)); // 타임아웃은 환경에 맞춰 조절
    }
}
