package com.example.demo.entity;

import com.example.demo.dto.request.UserUpdateRequestDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "app_user")
public class User extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;
    private String nickname;
    private String phone;
    private String profileImage;

    private String socialId;
    private String accountNumber;
    private String bank;

    private String address;

    private String preferredDong;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private int ecoScore = 0;
    private int coin = 0;

    @ElementCollection(targetClass = Theme.class)
    @CollectionTable(name = "user_preferred_themes", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "theme")
    @Enumerated(EnumType.STRING)
    private Set<Theme> preferredThemes = new HashSet<>();

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateOnboardingInfo(String preferredDong, Set<Theme> themes) {
        this.preferredDong=preferredDong;
        this.preferredThemes.clear();
        this.preferredThemes.addAll(themes);
    }

    // User.java 엔티티 내부
    public void updateMypageInfo(UserUpdateRequestDto dto) {
        // 닉네임이 요청에 포함된 경우에만 업데이트
        if (dto.getNickname() != null) {
            this.nickname = dto.getNickname();
        }
        // 프로필 이미지가 요청에 포함된 경우에만 업데이트
        if (dto.getProfileImage() != null) {
            this.profileImage = dto.getProfileImage();
        }
        // 전화번호가 요청에 포함된 경우에만 업데이트
        if (dto.getPhoneNumber() != null) {
            this.phone = dto.getPhoneNumber();
        }
        // 은행 정보가 요청에 포함된 경우에만 업데이트
        if (dto.getBank() != null) {
            this.bank = dto.getBank();
        }
        // 계좌번호가 요청에 포함된 경우에만 업데이트
        if (dto.getAccountNumber() != null) {
            this.accountNumber = dto.getAccountNumber();
        }
        // 선호 지역(동)이 요청에 포함된 경우에만 업데이트
        if (dto.getPreferredDong() != null) {
            this.preferredDong = dto.getPreferredDong();
        }

        // 선호 테마 업데이트 (이 부분은 이미 null 체크가 구현되어 있어 좋습니다!)
        if (dto.getPreferredThemes() != null) {
            this.preferredThemes.clear();
            this.preferredThemes.addAll(dto.getPreferredThemes());
        }
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
    public void updateEcoScore(int ecoScore) {
        this.ecoScore += ecoScore;
    }
    public void updateCoin(int coin){this.coin += coin;}
}
