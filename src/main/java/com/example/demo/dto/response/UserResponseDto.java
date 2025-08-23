package com.example.demo.dto.response;

import com.example.demo.entity.Theme;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class UserResponseDto {
    //닉넴,프사,동네,점수,계좌
    private String name;
    private String nickname;
    private String profileImage;
    private String address;
    private int ecoScore;
    private String bank;
    private String accountNumber;
    private String phoneNumber;
    private String addressSido;
    private String addressSigungu;
    private String addressDong;
    private Set<Theme> preferredThemes;
}
