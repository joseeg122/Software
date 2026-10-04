package com.fintrack.repository;

import com.fintrack.entity.Credit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditRepository extends JpaRepository<Credit, Long> {
    List<Credit> findByPersonIdOrderById(Long personId);

    List<Credit> findByPersonIdAndBankId(Long personId, Long bankId);
}
