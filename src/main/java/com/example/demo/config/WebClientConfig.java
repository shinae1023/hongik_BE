package com.example.demo.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${chatbot.base-url}")
    private String chatbotBaseUrl;

    @Bean
    public WebClient chatbotWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(chatbotBaseUrl)
                .build();
    }
}
