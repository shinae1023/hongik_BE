package com.example.demo.dto.response;

import com.example.demo.entity.Theme;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseLogin {
    private String accessToken;
    private String refreshToken;
    private String nickname;
    private String preferredDong;
    private Set<Theme> preferredThemes;
    private int ecoScore;
}
