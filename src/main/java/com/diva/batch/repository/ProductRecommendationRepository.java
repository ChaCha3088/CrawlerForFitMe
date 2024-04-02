package com.diva.batch.repository;

import com.diva.batch.fitme.entity.ProductRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRecommendationRepository extends
    JpaRepository<ProductRecommendation, Long> {

}
