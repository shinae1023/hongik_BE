package com.example.demo.controller;

import com.example.demo.dto.request.RequestSignup;
import com.example.demo.dto.request.RequestLogin;
import com.example.demo.dto.response.ResponseLogin;
import com.example.demo.service.UserSignupService;
import com.example.demo.service.UserLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final UserSignupService userSignupService;
    private final UserLoginService userLoginService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody @Valid RequestSignup request) {
        userSignupService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 완료");
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseLogin> login(@RequestBody @Valid RequestLogin request) {
        ResponseLogin response = userLoginService.authenticate(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }
}
