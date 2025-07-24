package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Farm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private String address;

    private String rentalPeriod;

    private Integer price;

    private Boolean isAvailable;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private User provider;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user; //텃밭 등록자 (판매자)

    @Builder
    public Farm(String title, String description, String address, String rentalPeriod, Integer price,
                boolean isAvailable, LocalDateTime createdAt, User user) {
        this.title = title;
        this.description = description;
        this.address = address;
        this.rentalPeriod = rentalPeriod;
        this.price = price;
        this.isAvailable = isAvailable;
        this.createdAt = createdAt;
        this.user = user;
    }
}
