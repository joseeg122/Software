package com.fintrack.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DashboardDto(Long personId, String fullName, String document, String overallStatus,
                           LocalDateTime lastCheckedAt, long banksConnected, long accounts, BigDecimal totalDebt,
                           long activeCredits, long processes, long findings, Integer score, Integer maxScore,
                           long unavailableSources) {
}
