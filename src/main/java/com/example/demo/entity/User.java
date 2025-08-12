package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
public class User extends BaseEntity {
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
    @Column(name = "address_sido")
    private String addressSido;
    @Column(name = "address_sigungu")
    private String addressSigungu;
    @Column(name = "address_dong")
    private String addressDong;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private int ecoScore;

    @ElementCollection(targetClass = Theme.class)
    @CollectionTable(name = "user_preferred_themes", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "theme")
    @Enumerated(EnumType.STRING)
    private Set<Theme> preferredThemes = new HashSet<>();

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateOnboardingInfo(String sido, String sigungu, String dong, Set<Theme> themes) {
        this.addressSido = sido;
        this.addressSigungu = sigungu;
        this.addressDong = dong;

        this.address = String.format("%s %s %s", sido, sigungu, dong);
        this.preferredThemes.clear();
        this.preferredThemes.addAll(themes);
    }
}
