package com.example.demo.service;

import com.example.demo.config.jwt.TokenProvider;
import com.example.demo.dto.response.ResponseLogin;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserLoginService {
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public ResponseLogin authenticate(String email, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        String accessToken = tokenProvider.createAccessToken(authentication);

        String refreshToken = tokenProvider.createRefreshToken(authentication);
        long refreshTokenExpiry = tokenProvider.getExpiration(refreshToken);

        refreshTokenRepository.findByUserEmail(email).ifPresentOrElse(
                token -> token.updateToken(refreshToken, refreshTokenExpiry),
                () -> refreshTokenRepository.save(RefreshToken.builder()
                        .userEmail(email)
                        .token(refreshToken)
                        .expiryDate(refreshTokenExpiry)
                        .build())
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + email));

        return ResponseLogin.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .nickname(user.getNickname())
                .preferredDong(user.getPreferredDong())
                .preferredThemes(user.getPreferredThemes())
                .ecoScore(user.getEcoScore())
                .money(user.getCoin())
                .build();
    }
}
