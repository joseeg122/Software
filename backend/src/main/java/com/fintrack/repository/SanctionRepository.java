package com.fintrack.repository;

import com.fintrack.entity.Sanction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SanctionRepository extends JpaRepository<Sanction, Long> {
    List<Sanction> findByPersonIdOrderById(Long personId);
}
