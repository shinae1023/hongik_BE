package com.example.demo.exception;

/**
 * 사용자의 코인이 결제 금액에 비해 부족할 때 발생하는 예외 클래스입니다.
 */
public class InsufficientCoinException extends RuntimeException {
    public InsufficientCoinException(String message) {
        super(message);
    }
}

