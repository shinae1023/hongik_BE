package com.example.demo.dto.response;

import lombok.*;

import java.util.List;

@Builder
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeResponseDto {
    private String message;
    private String bank;
    private String accountNumber;// 사용자의 계좌 목록
}
