package com.fintrack.repository;

import com.fintrack.entity.Source;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceRepository extends JpaRepository<Source, Long> {
    List<Source> findAllByOrderById();

    Optional<Source> findByCode(String code);
}
