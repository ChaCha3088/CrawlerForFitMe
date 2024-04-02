package com.diva.batch.fitme.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class ProductRecommendation {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @JoinColumn(name = "product_id")
    @ManyToOne(fetch = LAZY)
    private Product product;

    @JoinColumn(name = "recommendation_id")
    @ManyToOne(fetch = LAZY)
    private Product recommendation;

    @Builder
    private ProductRecommendation(Product product, Product recommendation) {
        this.product = product;
        this.product.addRecommendation(this);

        this.recommendation = recommendation;
    }
}
