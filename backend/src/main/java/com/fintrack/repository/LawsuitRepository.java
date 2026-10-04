package com.fintrack.repository;

import com.fintrack.entity.Lawsuit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LawsuitRepository extends JpaRepository<Lawsuit, Long> {
    List<Lawsuit> findByPersonIdOrderById(Long personId);
}
