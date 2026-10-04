package com.fintrack.repository;

import com.fintrack.entity.CreditScore;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditScoreRepository extends JpaRepository<CreditScore, Long> {
    Optional<CreditScore> findFirstByPersonIdOrderByCalculatedAtDesc(Long personId);
}
