package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Setter
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

    private Integer rentalPeriod;

    private Integer price;

    private Integer size;

    private LocalDateTime createdAt;

    private Long borrowerId;

    private LocalDateTime updateTime;

    private int premiumCount; // 오늘 프리미엄 등록한 횟수

    private LocalDate lastPremiumDate; // 마지막으로 프리미엄 등록한 날짜

    // 카운트 초기화 또는 날짜 업데이트 로직
    public void checkAndResetPremiumCount() {
        LocalDate today = LocalDate.now();
        // 마지막 등록 날짜가 오늘이 아니면, 카운트를 0으로 리셋
        if (this.lastPremiumDate == null || !this.lastPremiumDate.isEqual(today)) {
            this.premiumCount = 0;
            this.lastPremiumDate = today;
        }
    }

    // 카운트 증가 로직
    public void increasePremiumCount() {
        this.premiumCount++;
    }

    @Enumerated(EnumType.STRING)
    private Theme theme;

    @Column(nullable = false)
    private boolean isAvailable = true;

    private boolean ownerAuth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @BatchSize(size = 100)
    private List<FarmImage> images = new ArrayList<>();

    public void addImage(FarmImage image) {
        this.images.add(image);
        image.setFarm(this);
    }

    public void updateTime(){
        this.updateTime = LocalDateTime.now();
    }

    public void updateBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }
}
