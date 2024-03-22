package com.diva.batch.entity.fitme;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Entity;
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
@NoArgsConstructor(access = PROTECTED)
public class ProductColor {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @JoinColumn(name = "product_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    private String color;

    @OneToMany(mappedBy = "productColor")
    private List<ProductSize> productSize = new ArrayList<>();

    @Builder
    private ProductColor(Product product, String color) {
        this.product = product;
        this.color = color;
    }

    // == 연관 관계 메서드 == //
    public void addProductSize(ProductSize productSize) {
        this.productSize.add(productSize);
    }
}
