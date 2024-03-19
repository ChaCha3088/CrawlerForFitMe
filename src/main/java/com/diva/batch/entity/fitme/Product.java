package com.diva.batch.entity.fitme;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<ProductLike> productLikes = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<ProductReview> productReviews = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<ProductTag> productTags;

    private String name;

    @Enumerated(EnumType.ORDINAL)
    private Gender gender;  // MALE = 0, FEMALE = 1, UNISEX = 2

    private String ageRange;

    private Integer price;

    private int likeCount = 0;

    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<ProductOption> productOptions;

    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<MainImage> mainImage;

    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<DetailImage> detailImage;

    @Builder
    public Product(Brand brand, Category category, String name, Gender gender, String ageRange,
                   List<ProductTag> productTags, Integer price, List<ProductOption> productOptions, List<MainImage> mainImages, List<DetailImage> detailImages) {
        this.brand = brand;
        this.category = category;
        this.name = name;
        this.gender = gender;
        this.ageRange = ageRange;
        this.price = price;
        this.productOptions = productOptions;
        this.mainImage = mainImages;
        this.detailImage = detailImages;
        this.productTags = productTags;
    }


    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", brand=" + brand +
                ", category=" + category +
                ", productLikes=" + productLikes +
                ", productReviews=" + productReviews +
                ", productTags=" + productTags +
                ", name='" + name + '\'' +
                ", gender=" + gender +
                ", ageRange='" + ageRange + '\'' +
                ", price=" + price +
                ", likeCount=" + likeCount +
                ", mainImage=" + mainImage +
                ", detailImage=" + detailImage +
                '}';
    }

    // == 연관관계 메소드 == //
    public void addMainImage(MainImage mainImage) {
        this.mainImage.add(mainImage);
    }

    public void addDetailImage(DetailImage detailImage) {
        this.detailImage.add(detailImage);
    }
}
