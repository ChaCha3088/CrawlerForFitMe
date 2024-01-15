package com.diva.batch.repository;

import com.diva.batch.entity.Youtube;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YoutubeRepository extends JpaRepository<Youtube, Long> {

}
