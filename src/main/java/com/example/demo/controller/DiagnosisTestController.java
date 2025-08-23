package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class DiagnosisTestController {

    private final WebClient diagnosisAiWebClient;

    /**
     * AI 서버에 직접 테스트 요청을 보내는 엔드포인트
     */
    @PostMapping(value = "/direct-diagnose", consumes = "multipart/form-data")
    public Mono<ResponseEntity<Object>> testDirectDiagnose(
            @RequestParam("image") MultipartFile imageFile) {

        log.info("Testing direct diagnosis with file: {}, size: {}, type: {}",
                imageFile.getOriginalFilename(), imageFile.getSize(), imageFile.getContentType());

        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            ByteArrayResource resource = new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            };

            body.add("image", resource);

            return diagnosisAiWebClient.post()
                    .uri("/diagnose")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(Object.class)
                    .map(response -> {
                        log.info("Direct AI Response: {}", response);
                        return ResponseEntity.ok(response);
                    })
                    .onErrorResume(ex -> {
                        log.error("Direct diagnosis failed", ex);
                        return Mono.just(ResponseEntity.status(500)
                                .body(Map.of("error", ex.getMessage())));
                    });

        } catch (Exception e) {
            log.error("Exception in direct diagnosis", e);
            return Mono.just(ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage())));
        }
    }

    /**
     * AI 서버의 상태를 확인하는 엔드포인트
     */
    @GetMapping("/ai-health")
    public Mono<ResponseEntity<Object>> testAiHealth() {
        return diagnosisAiWebClient.get()
                .uri("/health")  // 또는 "/"
                .retrieve()
                .bodyToMono(Object.class)
                .map(ResponseEntity::ok)
                .onErrorResume(ex -> {
                    log.error("AI health check failed", ex);
                    return Mono.just(ResponseEntity.status(500)
                            .body(Map.of("error", "AI server is not accessible: " + ex.getMessage())));
                });
    }

    /**
     * AI 서버에 다양한 형태로 테스트 요청
     */
    @PostMapping(value = "/test-formats", consumes = "multipart/form-data")
    public Mono<ResponseEntity<Object>> testDifferentFormats(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(value = "format", defaultValue = "1") String format) {

        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            switch (format) {
                case "1": // 기본 형태
                    body.add("image", new ByteArrayResource(imageFile.getBytes()) {
                        @Override
                        public String getFilename() {
                            return imageFile.getOriginalFilename();
                        }
                    });
                    break;

                case "2": // file 파라미터로 시도
                    body.add("file", new ByteArrayResource(imageFile.getBytes()) {
                        @Override
                        public String getFilename() {
                            return imageFile.getOriginalFilename();
                        }
                    });
                    break;

                case "3": // 추가 메타데이터와 함께
                    body.add("image", new ByteArrayResource(imageFile.getBytes()) {
                        @Override
                        public String getFilename() {
                            return imageFile.getOriginalFilename();
                        }
                    });
                    body.add("content_type", imageFile.getContentType());
                    break;
            }

            return diagnosisAiWebClient.post()
                    .uri("/diagnose")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(Object.class)
                    .map(ResponseEntity::ok)
                    .onErrorResume(ex -> {
                        log.error("Format {} test failed", format, ex);
                        return Mono.just(ResponseEntity.status(500)
                                .body(Map.of("error", "Format " + format + " failed: " + ex.getMessage())));
                    });

        } catch (Exception e) {
            return Mono.just(ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage())));
        }
    }
}
