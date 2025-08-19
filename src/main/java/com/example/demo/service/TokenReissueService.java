package com.example.demo.service;

import com.example.demo.config.jwt.TokenProvider;
import com.example.demo.dto.response.ResponseReissueToken;
import com.example.demo.entity.RefreshToken;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.security.UserInfo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenReissueService {
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserInfoService userInfoService;

    @Transactional
    public ResponseReissueToken reissue(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.getTokenType(refreshToken).equals("refresh")) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 Refresh Token 입니다.");
        }

        String userEmail = tokenProvider.getUserEmailFromToken(refreshToken);

        RefreshToken storedRefreshToken = refreshTokenRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("저장된 Refresh Token이 없습니다. 다시 로그인 해주세요."));

        if (!storedRefreshToken.getToken().equals(refreshToken)) {
            refreshTokenRepository.delete(storedRefreshToken);
            throw new IllegalArgumentException("Refresh Token이 일치하지 않습니다. 다시 로그인 해주세요.");
        }

        UserInfo userInfo = (UserInfo) userInfoService.loadUserByUsername(userEmail);
        Authentication authentication = tokenProvider.getAuthentication(refreshToken);

        String newAccessToken = tokenProvider.createAccessToken(authentication);

        String newRefreshToken = tokenProvider.createRefreshToken(authentication);
        long newRefreshTokenExpiry = tokenProvider.getExpiration(newRefreshToken);

        storedRefreshToken.updateToken(newRefreshToken, newRefreshTokenExpiry);
        refreshTokenRepository.save(storedRefreshToken);

        return ResponseReissueToken.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Transactional
    public void deleteRefreshToken(String userEmail) {
        refreshTokenRepository.deleteByUserEmail(userEmail);
    }

    public String getUserEmailFromRefreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.getTokenType(refreshToken).equals("refresh")) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 Refresh Token 입니다.");
        }
        return tokenProvider.getUserEmailFromToken(refreshToken);
    }
}
