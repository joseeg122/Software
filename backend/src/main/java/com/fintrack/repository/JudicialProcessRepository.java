package com.fintrack.repository;

import com.fintrack.entity.JudicialProcess;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JudicialProcessRepository extends JpaRepository<JudicialProcess, Long> {
    List<JudicialProcess> findByPersonIdOrderById(Long personId);
}
