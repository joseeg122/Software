package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "legal_measures")
public class LegalMeasure extends BaseEntity {
    public Long personId;
    public Long processId;
    public String measureType;
    public String asset;
    public BigDecimal amount;
    public String status;
    public LocalDate orderedAt;
}
