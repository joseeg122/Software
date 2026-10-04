package com.fintrack.repository;

import com.fintrack.entity.TrafficRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrafficRecordRepository extends JpaRepository<TrafficRecord, Long> {
    List<TrafficRecord> findByPersonIdOrderById(Long personId);
}
