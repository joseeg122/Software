package com.fintrack.repository;

import com.fintrack.entity.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPersonIdOrderByCreatedAtAsc(Long personId);
}
