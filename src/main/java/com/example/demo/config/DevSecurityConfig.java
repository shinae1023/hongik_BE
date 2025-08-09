package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("dev") // 이 설정은 'dev' 프로필에서만 활성화됩니다.
public class DevSecurityConfig {

    // 'dev' 프로필에서도 PasswordEncoder는 반드시 필요합니다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 개발 환경에서는 모든 요청을 허용하여 테스트를 쉽게 할 수 있습니다.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll()) // 모든 요청 허용
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // H2 콘솔 접근 허용

        return http.build();
    }
}