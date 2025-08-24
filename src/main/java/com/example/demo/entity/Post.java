package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String content;


    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "view_count")
    private Long viewCount;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "modified_at")
    private Timestamp modifiedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // 빌더 패턴 사용 시 초기화 보장
    private Set<Image> images = new HashSet<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Like> likes = new HashSet<>();

    public void addImage(Image image) {
        this.images.add(image);
    }

}
