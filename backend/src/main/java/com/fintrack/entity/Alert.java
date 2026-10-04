package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class Alert extends BaseEntity {
    public Long personId;
    @Enumerated(EnumType.STRING)
    public Severity severity;
    public String title;
    public String description;
    public String category;
    public LocalDateTime createdAt;
    public String status;
}
