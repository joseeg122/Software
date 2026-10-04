package com.fintrack.repository;

import com.fintrack.entity.Alert;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByPersonIdOrderByCreatedAtDesc(Long personId);
}
