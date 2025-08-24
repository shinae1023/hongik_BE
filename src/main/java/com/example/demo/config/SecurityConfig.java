package com.example.demo.config;

import com.example.demo.config.jwt.CustomJwtFilter;
import com.example.demo.config.jwt.JwtAccessDeniedHandler;
import com.example.demo.config.jwt.JwtAuthenticationEntryPoint;
import com.example.demo.service.KakaoOAuth2UserService;
import com.example.demo.service.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.Arrays;

@Profile("!dev")
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KakaoOAuth2UserService kakaoOAuth2UserService;
    private final CustomJwtFilter customJwtFilter;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    // ✅ 1. 공개 경로용 SecurityFilterChain (JWT 필터 없음)
    @Bean
    @Order(1)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                                .frameOptions(frameOptions -> frameOptions.disable()) // h2-console 등을 위해 disable하거나
                        // .frameOptions(frameOptions -> frameOptions.sameOrigin()) // 혹은 sameOrigin()으로 설정
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 이 필터 체인이 적용될 경로 지정
                .securityMatcher(
                        "/", "/home", "/login/**", "/oauth2/**", "/h2-console/**",
                        "/api/auth/**", "/static/**", "/favicon.ico", "/auth", "/Signup",
                        "/css/**", "/js/**", "/images/**", "/products/**", "/ws-chat/**",
                        "/api/chat/**", "/ws-chat", "/farm/**"
                )
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());

        return http.build();
    }

    // ✅ 2. 인증이 필요한 API용 SecurityFilterChain (JWT 필터 적용)
    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 이 필터 체인이 적용될 경로 지정
                .securityMatcher("/api/v1/**", "/reviews/**", "/posts/**", "/comment/**",
                        "/mypage/**", "/chat/**", "/like/**", "/pay/**", "/profile/**", "/api/chatbot/**")
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.POST, "/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/comment").authenticated()
                        .requestMatchers(HttpMethod.PATCH,"/mypage/**").authenticated()
                        .requestMatchers(HttpMethod.POST,"/mypage/**").authenticated()
                        .requestMatchers(HttpMethod.GET,"/mypage/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/chat/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/chat/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/chat/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/like/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/like/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/pay/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/pay/**").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                // 이 체인에만 JWT 필터를 추가
                .addFilterBefore(customJwtFilter, UsernamePasswordAuthenticationFilter.class)
                // OAuth2 관련 설정은 필요에 따라 이 체인 또는 다른 체인에 구성할 수 있습니다.
                // 만약 /api/v1/** 경로 외에 OAuth2가 필요하다면 publicFilterChain에도 추가해야 할 수 있습니다.
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(kakaoOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "https://spacefarm.shop",
                "http://localhost:8080",
                "https://jiangxy.github.io",
                "https://www.spacefarm.cloud", "https://spacefarm-chatbot-app.fly.dev", "http://shinae/fly.dev"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        // WebSocket 관련 헤더 추가
        configuration.addExposedHeader("Sec-WebSocket-Accept");
        configuration.addExposedHeader("Sec-WebSocket-Extensions");
        configuration.addExposedHeader("Sec-WebSocket-Protocol");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
