package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto {
    //닉넴,프사,동네,점수,계좌
    private String nickname;
    private String imageUrl;
    private String address;
    private int ecoScore;
    private String bank;
    private String accountNumber;
}
