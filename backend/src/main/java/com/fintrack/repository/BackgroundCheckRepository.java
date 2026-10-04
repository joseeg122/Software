package com.fintrack.repository;

import com.fintrack.entity.BackgroundCheck;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BackgroundCheckRepository extends JpaRepository<BackgroundCheck, Long> {
    List<BackgroundCheck> findByPersonIdOrderById(Long personId);
}
