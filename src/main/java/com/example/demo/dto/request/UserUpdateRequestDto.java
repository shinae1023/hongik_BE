package com.example.demo.dto.request;

import com.example.demo.entity.Theme;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {
    private String nickname;
    private String profileImage;
    private String phone;
    private String bank;
    private String accountNumber;
    private String addressSido;
    private String addressSigungu;
    private String addressDong;
    private Set<Theme> preferredThemes;
}
