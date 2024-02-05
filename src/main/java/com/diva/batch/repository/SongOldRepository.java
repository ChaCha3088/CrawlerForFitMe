package com.diva.batch.repository;

import com.diva.batch.entity.SongOld;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongOldRepository extends JpaRepository<SongOld, Long> {
}
