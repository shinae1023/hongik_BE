package com.example.demo.service;

import com.example.demo.config.jwt.TokenProvider;
import com.example.demo.security.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        log.info("OAuth2 로그인 성공, JWT 발급 시작");

        // 1) Authentication에서 UserInfo 꺼내기
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        // 2) TokenProvider로 JWT 생성
        String accessToken = tokenProvider.createToken(authentication);

        // 3) JSON 응답 작성
        Map<String, Object> resp = Map.of(
                "success", true,
                "accessToken", accessToken
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(resp));
        response.getWriter().flush();

        log.info("JWT 발급 완료: {}", accessToken);
    }
}
