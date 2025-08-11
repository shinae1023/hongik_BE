package com.example.demo.dto.request;

import com.example.demo.entity.Theme;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class OnboardingRequestDto {
    @NotNull(message = "주소 정보는 필수입니다.")
    @Valid
    private AddressDto address;

    @NotEmpty(message = "선호 테마는 최소 하나 이상 선택해야 합니다.")
    private List<Theme> themes;

    public AddressDto getAddress() {
        return address;
    }

    public void setAddress(AddressDto address) {
        this.address = address;
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
    }

    public static class AddressDto {
        @NotBlank(message = "시/도는 필수입니다.")
        private String sido;

        @NotBlank(message = "시/군/구는 필수입니다.")
        private String sigungu;

        @NotBlank(message = "읍/면/동은 필수입니다.")
        private String dong;

        public String getSido() {
            return sido;
        }

        public void setSido(String sido) {
            this.sido = sido;
        }

        public String getSigungu() {
            return sigungu;
        }

        public void setSigungu(String sigungu) {
            this.sigungu = sigungu;
        }

        public String getDong() {
            return dong;
        }

        public void setDong(String dong) {
            this.dong = dong;
        }
    }
}
