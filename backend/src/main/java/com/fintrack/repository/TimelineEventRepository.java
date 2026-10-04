package com.fintrack.repository;

import com.fintrack.entity.TimelineEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimelineEventRepository extends JpaRepository<TimelineEvent, Long> {
    List<TimelineEvent> findTop10ByPersonIdOrderByOccurredAtDesc(Long personId);
}
