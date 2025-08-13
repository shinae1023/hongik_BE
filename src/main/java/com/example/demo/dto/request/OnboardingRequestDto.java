package com.example.demo.dto.request;

import com.example.demo.entity.Theme;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OnboardingRequestDto {
    @NotNull(message = "선호 동네는 필수 항목입니다.") // Added message for clarity
    @NotBlank(message = "선호 동네는 비어 있을 수 없습니다.")
    private String preferredDong;

    @NotEmpty(message = "선호 테마는 최소 하나 이상 선택해야 합니다.")
    private List<Theme> themes;

}
