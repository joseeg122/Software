package com.fintrack.repository;

import com.fintrack.entity.CreditHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditHistoryRepository extends JpaRepository<CreditHistory, Long> {
    List<CreditHistory> findByPersonIdOrderByEventDateDesc(Long personId);
}
