package com.diva.batch.repository;

import com.diva.batch.fitme.entity.Brand;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long>, BrandRepositoryQueryDsl {
    Optional<Brand> findByName(String name);
}
