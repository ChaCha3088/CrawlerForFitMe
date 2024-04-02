package com.diva.batch.fitme.entity;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @JoinColumn(name = "brand_id")
    @ManyToOne(fetch = LAZY)
    private Brand brand;

    @JoinColumn(name = "category_id")
    @ManyToOne(fetch = LAZY)
    private Category category;

    @ColumnDefault("0")
    private int reviewCount = 0;

    @ColumnDefault("0.0")
    private double reviewRating = 0.0;

    private String name;

    @Enumerated(EnumType.ORDINAL)
    private Gender gender;  // MALE = 0, FEMALE = 1, UNISEX = 2

    private String ageRange;

    private Integer price;

    private int likeCount = 0;

    private int monthlyPopularityScore = 0;

    @OneToMany(mappedBy = "product", cascade = PERSIST, orphanRemoval = true)
    private List<ProductRecommendation> productRecommendations = new ArrayList<>();

    @Builder
    public Product(Brand brand, Category category, String name, Gender gender, String ageRange,
        Integer price) {
        this.brand = brand;
        this.category = category;
        this.name = name;
        this.gender = gender;
        this.ageRange = ageRange;
        this.price = price;
    }

    // == 연관관계 메소드 == //
    public void addRecommendation(ProductRecommendation productRecommendation) {
        this.productRecommendations.add(productRecommendation);
    }
}
