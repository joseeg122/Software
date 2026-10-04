package com.fintrack.repository;

import com.fintrack.entity.ReputationalNews;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReputationalNewsRepository extends JpaRepository<ReputationalNews, Long> {
    List<ReputationalNews> findByPersonIdOrderById(Long personId);
}
