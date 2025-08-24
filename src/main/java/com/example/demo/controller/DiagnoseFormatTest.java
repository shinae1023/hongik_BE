package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/diagnose-test")
@RequiredArgsConstructor
public class DiagnoseFormatTest {

    private final WebClient diagnosisAiWebClient;

    /**
     * /diagnose 엔드포인트가 GET을 지원하는지 확인
     */
    @GetMapping("/check-get")
    public Mono<ResponseEntity<Map<String, Object>>> checkDiagnoseGet() {
        log.info("Testing GET /diagnose");

        return diagnosisAiWebClient.get()
                .uri("/diagnose")
                .retrieve()
                .bodyToMono(Object.class)
                .map(response -> {
                    log.info("GET /diagnose response: {}", response);
                    return ResponseEntity.ok(Map.of(
                            "method", "GET",
                            "endpoint", "/diagnose",
                            "status", "SUCCESS",
                            "response", response
                    ));
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.info("GET /diagnose failed - Status: {}, Body: {}",
                            ex.getStatusCode(), ex.getResponseBodyAsString());
                    return Mono.just(ResponseEntity.ok(Map.of(
                            "method", "GET",
                            "endpoint", "/diagnose",
                            "status", "FAILED",
                            "error_code", ex.getStatusCode().toString(),
                            "error_body", ex.getResponseBodyAsString()
                    )));
                })
                .onErrorResume(ex -> {
                    return Mono.just(ResponseEntity.ok(Map.of(
                            "method", "GET",
                            "endpoint", "/diagnose",
                            "status", "ERROR",
                            "error", ex.getMessage()
                    )));
                });
    }

    /**
     * POST /diagnose에 빈 요청을 보내서 어떤 에러가 나오는지 확인
     */
    @PostMapping("/check-post-empty")
    public Mono<ResponseEntity<Map<String, Object>>> checkDiagnosePostEmpty() {
        log.info("Testing POST /diagnose with empty body");

        return diagnosisAiWebClient.post()
                .uri("/diagnose")
                .retrieve()
                .bodyToMono(Object.class)
                .map(response -> {
                    log.info("POST /diagnose (empty) response: {}", response);
                    return ResponseEntity.ok(Map.of(
                            "method", "POST",
                            "body", "empty",
                            "status", "SUCCESS",
                            "response", response
                    ));
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.info("POST /diagnose (empty) failed - Status: {}, Body: {}",
                            ex.getStatusCode(), ex.getResponseBodyAsString());
                    return Mono.just(ResponseEntity.ok(Map.of(
                            "method", "POST",
                            "body", "empty",
                            "status", "FAILED",
                            "error_code", ex.getStatusCode().toString(),
                            "error_body", ex.getResponseBodyAsString(),
                            "analysis", analyzeError(ex.getStatusCode().value(), ex.getResponseBodyAsString())
                    )));
                })
                .onErrorResume(ex -> {
                    return Mono.just(ResponseEntity.ok(Map.of(
                            "method", "POST",
                            "body", "empty",
                            "status", "ERROR",
                            "error", ex.getMessage()
                    )));
                });
    }

    /**
     * 다양한 파라미터 이름으로 테스트 (실제 이미지 없이 더미 데이터로)
     */
    @PostMapping("/test-parameter-names")
    public Mono<ResponseEntity<Map<String, Object>>> testParameterNames() {
        log.info("Testing different parameter names for /diagnose");

        List<String> paramNames = List.of("image", "file", "photo", "picture", "upload", "data");

        return reactor.core.publisher.Flux.fromIterable(paramNames)
                .flatMap(paramName -> testWithDummyFile(paramName))
                .collectList()
                .map(results -> ResponseEntity.ok(Map.of(
                        "test", "parameter_names",
                        "total_tested", paramNames.size(),
                        "results", results
                )));
    }

    /**
     * 실제 이미지 파일로 다양한 형식 테스트
     */
    @PostMapping(value = "/test-with-real-file", consumes = "multipart/form-data")
    public Mono<ResponseEntity<Map<String, Object>>> testWithRealFile(
            @RequestParam("testImage") MultipartFile imageFile) {

        log.info("Testing /diagnose with real file: {}, size: {}, type: {}",
                imageFile.getOriginalFilename(), imageFile.getSize(), imageFile.getContentType());

        List<String> paramNames = List.of("image", "file", "photo", "picture");

        return reactor.core.publisher.Flux.fromIterable(paramNames)
                .flatMap(paramName -> testWithRealFile(paramName, imageFile))
                .collectList()
                .map(results -> ResponseEntity.ok(Map.of(
                        "test", "real_file",
                        "file_info", Map.of(
                                "name", imageFile.getOriginalFilename(),
                                "size", imageFile.getSize(),
                                "type", imageFile.getContentType()
                        ),
                        "results", results
                )));
    }

    /**
     * OPTIONS 요청으로 CORS 및 허용된 메소드 확인
     */
    @GetMapping("/check-options")
    public Mono<ResponseEntity<Map<String, Object>>> checkOptions() {
        log.info("Testing OPTIONS /diagnose");

        return diagnosisAiWebClient.options()
                .uri("/diagnose")
                .retrieve()
                .bodyToMono(Object.class)
                .map(response -> ResponseEntity.ok(Map.of(
                        "method", "OPTIONS",
                        "status", "SUCCESS",
                        "response", response
                )))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just(ResponseEntity.ok(Map.of(
                            "method", "OPTIONS",
                            "status", "FAILED",
                            "error_code", ex.getStatusCode().toString(),
                            "headers", ex.getHeaders().toString()
                    )));
                });
    }

    private Mono<Map<String, Object>> testWithDummyFile(String paramName) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // 더미 이미지 데이터 (1x1 픽셀 PNG)
            byte[] dummyImage = new byte[]{
                    (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                    0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52
            };

            ByteArrayResource resource = new ByteArrayResource(dummyImage) {
                @Override
                public String getFilename() {
                    return "test.png";
                }
            };

            body.add(paramName, resource);

            return diagnosisAiWebClient.post()
                    .uri("/diagnose")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(Object.class)
                    .map(response -> Map.of(
                            "param_name", paramName,
                            "status", "SUCCESS",
                            "response", response.toString().length() > 100 ?
                                    response.toString().substring(0, 100) + "..." : response
                    ))
                    .onErrorResume(WebClientResponseException.class, ex ->
                            Mono.just(Map.of(
                                    "param_name", paramName,
                                    "status", "FAILED",
                                    "error_code", ex.getStatusCode().toString(),
                                    "error_body", ex.getResponseBodyAsString()
                            ))
                    )
                    .onErrorResume(ex ->
                            Mono.just(Map.of(
                                    "param_name", paramName,
                                    "status", "ERROR",
                                    "error", ex.getMessage()
                            ))
                    );
        } catch (Exception e) {
            return Mono.just(Map.of(
                    "param_name", paramName,
                    "status", "EXCEPTION",
                    "error", e.getMessage()
            ));
        }
    }

    private Mono<Map<String, Object>> testWithRealFile(String paramName, MultipartFile imageFile) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            ByteArrayResource resource = new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            };

            body.add(paramName, resource);

            return diagnosisAiWebClient.post()
                    .uri("/diagnose")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(Object.class)
                    .map(response -> {
                        log.info("SUCCESS with param '{}': {}", paramName, response);
                        return Map.of(
                                "param_name", paramName,
                                "status", "SUCCESS",
                                "response", response
                        );
                    })
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        log.info("FAILED with param '{}' - Status: {}, Body: {}",
                                paramName, ex.getStatusCode(), ex.getResponseBodyAsString());
                        return Mono.just(Map.of(
                                "param_name", paramName,
                                "status", "FAILED",
                                "error_code", ex.getStatusCode().toString(),
                                "error_body", ex.getResponseBodyAsString()
                        ));
                    });
        } catch (Exception e) {
            return Mono.just(Map.of(
                    "param_name", paramName,
                    "status", "EXCEPTION",
                    "error", e.getMessage()
            ));
        }
    }

    private String analyzeError(int statusCode, String errorBody) {
        if (statusCode == 400) {
            if (errorBody.contains("image") || errorBody.contains("file")) {
                return "파일 파라미터가 필요한 것 같습니다";
            } else if (errorBody.contains("multipart")) {
                return "multipart/form-data 형식이 필요한 것 같습니다";
            } else {
                return "요청 형식 오류 - 필수 파라미터 누락 가능성";
            }
        } else if (statusCode == 405) {
            return "POST 메소드가 허용되지 않습니다";
        } else if (statusCode == 415) {
            return "지원되지 않는 미디어 타입입니다";
        }
        return "분석 중...";
    }
}