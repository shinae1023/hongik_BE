package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentReadyDto {
    private Long chatroomId;
    private String farmTitle;
    private Integer price;
    private String rentalPeriod;

    private String providerNickname;
    private String bank;
    private String accountNumber;
}
