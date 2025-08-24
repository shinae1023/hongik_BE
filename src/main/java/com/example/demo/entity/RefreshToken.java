package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "refresh_token")
@Builder

public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userEmail;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(nullable = false)
    private long expiryDate;

    public void updateToken(String newToken, long newExpiryDate) {
        this.token = newToken;
        this.expiryDate = newExpiryDate;
    }
}
