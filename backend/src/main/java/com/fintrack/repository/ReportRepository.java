package com.fintrack.repository;

import com.fintrack.entity.Report;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByPersonIdOrderByIdDesc(Long personId);
}
