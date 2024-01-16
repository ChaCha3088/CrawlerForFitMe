package com.diva.batch.repository;

import com.diva.batch.entity.YoutubeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YoutubeFileRepository extends JpaRepository<YoutubeFile, Long> {

}
