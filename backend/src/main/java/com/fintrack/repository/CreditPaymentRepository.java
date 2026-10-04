package com.fintrack.repository;

import com.fintrack.entity.CreditPayment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditPaymentRepository extends JpaRepository<CreditPayment, Long> {
    List<CreditPayment> findByPersonIdOrderByDueDateDescIdAsc(Long personId);
}
