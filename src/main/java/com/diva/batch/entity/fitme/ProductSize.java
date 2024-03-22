package com.diva.batch.entity.fitme;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
public class ProductSize {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @JoinColumn(name = "product_color_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductColor productColor;

    private String size;

    private int stockQuantity;

    @Builder
    private ProductSize(ProductColor productColor, String size, int stockQuantity) {
        this.productColor = productColor;
        this.productColor.addProductSize(this);

        this.size = size;
        this.stockQuantity = stockQuantity;
    }
}
