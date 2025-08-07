package com.example.demo.config;

import com.example.demo.config.jwt.JwtAccessDeniedHandler;
import com.example.demo.config.jwt.JwtAuthenticationEntryPoint;
import com.example.demo.global.JwtTokenFilter;
import com.example.demo.service.KakaoOAuth2UserService;
import com.example.demo.service.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KakaoOAuth2UserService kakaoOAuth2UserService;
    private final JwtTokenFilter jwtTokenFilter;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    /**
     * Spring Security 필터 체인 설정
     * 모든 보안 규칙을 정의하는 핵심 메서드
     *
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 보안 필터 체인
     * @throws Exception 설정 오류 시
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // === CSRF 보안 설정 ===
                // REST API에서는 CSRF 공격 위험이 적으므로 비활성화
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frameOptionsConfig -> frameOptionsConfig.disable()) // H2 콘솔 iframe 허용
                )

                // === CORS 설정 ===
                // 프론트엔드에서 API 호출 시 필요
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // === 세션 관리 설정 ===
                // JWT 토큰을 사용하므로 세션을 생성하지 않음
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 예외 처리 핸들러
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                // === 요청 권한 설정 ===
                .authorizeHttpRequests(authz -> authz
                        // 인증 없이 접근 가능한 경로들
                        .requestMatchers(
                                "/",                    // 메인 페이지
                                "/login/**",            // 로그인 관련
                                "/oauth2/**",          // OAuth2 관련
                                "/h2-console/**",      // H2 데이터베이스 콘솔
                                "/api/auth/**",   // 인증 관련 API
                                "/static/**",
                                "/home",
                                "/favicon.ico", "/auth",
                                "/Signup", "/css/**", "/js/**", "/images/**","/home",
                                "products/**"
                        ).permitAll()
                        .requestMatchers(
                                "/api/v1/auth/me",       // 현재 사용자 정보 조회
                                "/api/v1/auth/validate", // JWT 토큰 검증
                                "/api/v1/products/**",   // 상품 관련 - 인증 필요
                                "/api/v1/cart/**"        // 장바구니 관련 - 인증 필요
                        ).authenticated()

                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // === OAuth2 로그인 설정 ===
                .oauth2Login(oauth2 -> oauth2
                        // 로그인 페이지 URL
                        //.loginPage("/Signup")

                        // 카카오 사용자 정보 처리 서비스
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(kakaoOAuth2UserService)
                        )

                        // OAuth2 로그인 성공 시 처리 핸들러
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )

                // === 커스텀 필터 추가 ===
                // JWT 인증 필터를 Spring Security 필터 체인에 추가
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * CORS 설정
     * 프론트엔드(React, Vue 등)에서 백엔드 API 호출 시 필요
     *
     * @return CorsConfigurationSource CORS 설정 소스
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 도메인 설정 (개발환경)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",    // React 개발 서버
                "http://localhost:8080",     // 같은 서버
                "http://localhost:5173"
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 인증 정보 포함 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }


}
