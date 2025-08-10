package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "farm_image")
public class FarmImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    public void setFarm(Farm farm) { this.farm = farm; }

    public static FarmImage of(String imageUrl, Farm farm) {
        return FarmImage.builder()
                .imageUrl(imageUrl)
                .farm(farm)
                .build();
    }
}
