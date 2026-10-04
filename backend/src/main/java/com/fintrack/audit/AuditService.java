package com.fintrack.audit;

import com.fintrack.entity.AuditLog;
import com.fintrack.repository.AuditLogRepository;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void log(String action, String detail) {
        AuditLog entry = new AuditLog();
        entry.username = currentUser();
        entry.action = action;
        entry.detail = detail.length() > 255 ? detail.substring(0, 255) : detail;
        entry.createdAt = LocalDateTime.now();
        repository.save(entry);
    }

    public static String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || auth.getName() == null ? "sistema" : auth.getName();
    }
}
