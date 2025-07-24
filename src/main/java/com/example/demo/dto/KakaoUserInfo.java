package com.example.demo.dto;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Getter
@Slf4j
public class KakaoUserInfo {
    private String id;
    private String nickname;
    private String email;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.id = String.valueOf(attributes.get("id"));
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        this.nickname = (String) properties.get("nickname");
        Map<String, Object> kakao_account = (Map<String, Object>) attributes.get("kakao_account");
        this.email = (String) kakao_account.get("email");
    }
}
