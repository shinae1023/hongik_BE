package com.example.demo.config;


import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Configuration
public class WebClientConfig {

    @Value("${chatbot.base-url}")
    private String chatbotBaseUrl;

    @Value("${diagnosis.ai.base-url}") // 🟢 yml에 추가한 값 주입
    private String diagnosisAiBaseUrl;

    @Bean
    public WebClient chatbotWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(chatbotBaseUrl)
                .build();
    }

    @Bean
    public WebClient diagnosisAiWebClient(WebClient.Builder builder) throws SSLException {
        // 모든 SSL 인증서를 신뢰하도록 SslContext를 생성합니다.
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        // SslContext를 사용하는 HttpClient를 생성합니다.
        HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));

        return builder
                .baseUrl(diagnosisAiBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
