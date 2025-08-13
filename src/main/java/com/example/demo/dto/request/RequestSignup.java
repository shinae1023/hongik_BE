package com.example.demo.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestSignup {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;

    @NotBlank
    private String name;

    @NotBlank
    private String nickname;

    @NotBlank @Pattern(regexp="^\\d{2,3}-\\d{3,4}-\\d{4}$")
    private String phone;

    @NotBlank
    private String bank;

    @NotBlank
    private String accountNumber;

    @AssertTrue(message = "비밀번호와 비밀번호 확인이 일치하지 않습니다.")
    private boolean isPasswordsMatch() {
        if (password == null || confirmPassword == null) return false;
        return password.equals(confirmPassword);
    }
}
