package com.diva.batch.entity.fitme;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class MainImage extends BaseEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String url;

    @ManyToOne(fetch = LAZY)
    private Product product;

    @Builder
    private MainImage(String url, Product product) {
        this.url = url;
        this.product = product;
        this.product.addMainImage(this);
    }
}
