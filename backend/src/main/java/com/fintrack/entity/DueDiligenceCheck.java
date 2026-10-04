package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "due_diligence_checks")
public class DueDiligenceCheck extends BaseEntity {
    public Long personId;
    public int runNumber;
    public LocalDateTime startedAt;
    public LocalDateTime finishedAt;
    public String status;
    public int totalSources;
    public int withFindings;
    public int unavailable;
    public String triggeredBy;
}
