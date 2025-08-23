package com.example.demo.dto.response;

import lombok.*;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountResponseDto {
    private String bank;
    private String accountNumber;
}
