package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiseaseDiagnosisService {

    private final WebClient diagnosisAiWebClient;
    private final ChatbotService chatbotService;
    private final ImageConverterService imageConverterService; // 새로 추가

    public Mono<String> diagnose(MultipartFile imageFile, String plantType) {
        // 입력 값 검증
        if (imageFile == null || imageFile.isEmpty()) {
            return Mono.error(new IllegalArgumentException("이미지 파일이 필요합니다."));
        }

        if (plantType == null || plantType.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("작물 종류를 입력해주세요."));
        }

        log.info("Starting diagnosis for plant type: {}, file size: {}", plantType, imageFile.getSize());

        return callDiagnosisAI(imageFile)
                .doOnNext(result -> log.info("Diagnosis result: {}", result))
                .flatMap(diagnosisResult -> {
                    String prompt = createLlmPrompt(diagnosisResult, plantType);
                    return chatbotService.ask(prompt);
                })
                .doOnError(error -> log.error("Diagnosis failed: ", error));
    }

    private Mono<String> callDiagnosisAI(MultipartFile imageFile) {
        try {
            // 파일 유효성 검사
            if (!isValidImageFile(imageFile)) {
                return Mono.error(new IllegalArgumentException("지원되지 않는 파일 형식입니다. (jpg, jpeg, png만 지원)"));
            }

            // 파일 크기 제한 (예: 10MB)
            if (imageFile.getSize() > 10 * 1024 * 1024) {
                return Mono.error(new IllegalArgumentException("파일 크기가 너무 큽니다. (최대 10MB)"));
            }

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // 🔥 핵심: 이미지를 RGB JPG 형식으로 변환
            ByteArrayResource resource = imageConverterService.convertToRGB(imageFile);
            body.add("file", resource);

            // 로깅 추가
            log.info("Sending request to diagnosis AI server with file: {}", imageFile.getOriginalFilename());

            return diagnosisAiWebClient.post()
                    .uri("/diagnose")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(responseMap -> {
                        log.info("AI Response: {}", responseMap);

                        // 응답 구조 확인 및 다양한 키 시도
                        String result = null;
                        if (responseMap.containsKey("result")) {
                            result = (String) responseMap.get("result");
                        } else if (responseMap.containsKey("disease_name")) {
                            result = (String) responseMap.get("disease_name");
                        } else if (responseMap.containsKey("diagnosis")) {
                            result = (String) responseMap.get("diagnosis");
                        } else if (responseMap.containsKey("prediction")) {
                            result = (String) responseMap.get("prediction");
                        } else {
                            log.warn("Unknown response format: {}", responseMap);
                            result = responseMap.toString();
                        }

                        return result != null ? result : "진단 결과를 확인할 수 없습니다.";
                    })
                    .onErrorMap(WebClientResponseException.class, ex -> {
                        log.error("AI Server Error - Status: {}, Body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
                        return new RuntimeException("AI 서버 오류: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
                    });

        } catch (IOException e) {
            log.error("Failed to process image file", e);
            return Mono.error(new RuntimeException("이미지 파일을 처리하는 중 오류가 발생했습니다: " + e.getMessage(), e));
        }
    }

    private boolean isValidImageFile(MultipartFile file) {
        if (file.getContentType() == null) {
            return false;
        }

        String contentType = file.getContentType().toLowerCase();
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/bmp");
    }

    private String createLlmPrompt(String diagnosisResult, String plantType) {
        if ("정상".equals(diagnosisResult) || "normal".equalsIgnoreCase(diagnosisResult)) {
            return String.format("AI가 %s 작물이 건강한 상태라고 진단했어. 사용자에게 축하해주고, 앞으로도 건강하게 키울 수 있는 일반적인 관리 팁을 알려줘.", plantType);
        }

        return String.format("농작물 진단 AI가 '%s' 사진을 보고 '%s'이라고 진단했어. 이 진단 결과를 바탕으로, 사용자에게 친절하고 상세하게 설명해줘. 설명에는 '%s'의 주요 원인, 초기 증상, 그리고 구체적인 해결 및 예방 방법을 단계별로 포함해줘.",
                plantType, diagnosisResult, diagnosisResult);
    }
}