package com.fintrack.repository;

import com.fintrack.entity.Transaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByPersonIdOrderByTxDateDesc(Long personId);
}
