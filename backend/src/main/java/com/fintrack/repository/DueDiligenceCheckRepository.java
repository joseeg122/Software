package com.fintrack.repository;

import com.fintrack.entity.DueDiligenceCheck;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DueDiligenceCheckRepository extends JpaRepository<DueDiligenceCheck, Long> {
    Optional<DueDiligenceCheck> findFirstByPersonIdOrderByIdDesc(Long personId);
}
