package com.diva.batch.repository;

import com.diva.batch.entity.fitme.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
