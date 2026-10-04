package com.fintrack.repository;

import com.fintrack.entity.PepRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PepRecordRepository extends JpaRepository<PepRecord, Long> {
    List<PepRecord> findByPersonIdOrderById(Long personId);
}
