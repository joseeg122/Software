package com.fintrack.repository;

import com.fintrack.entity.Bank;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<Bank, Long> {
    Optional<Bank> findBySlug(String slug);
}
