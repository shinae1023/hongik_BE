package com.example.demo.exception;

public class UserNotFoundException extends RuntimeException {
    // 기본 생성자
    public UserNotFoundException() {
        super();
    }

    // 예외 메시지를 받는 생성자
    public UserNotFoundException(String message) {
        super(message);
    }
}
