package com.diva.batch.repository;

import com.diva.batch.entity.fitme.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
