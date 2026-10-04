package com.fintrack.repository;

import com.fintrack.entity.LegalMeasure;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalMeasureRepository extends JpaRepository<LegalMeasure, Long> {
    List<LegalMeasure> findByPersonIdOrderById(Long personId);
}
