package com.fintrack.repository;

import com.fintrack.entity.SourceResult;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceResultRepository extends JpaRepository<SourceResult, Long> {
    List<SourceResult> findByPersonIdOrderBySourceIdAsc(Long personId);
}
