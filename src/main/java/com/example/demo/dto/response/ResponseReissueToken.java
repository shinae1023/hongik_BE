package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseReissueToken {
    private String accessToken;
    private String refreshToken;
}
