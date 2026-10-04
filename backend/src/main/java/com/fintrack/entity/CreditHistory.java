package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_history")
public class CreditHistory extends BaseEntity {
    public Long creditId;
    public Long personId;
    public LocalDate eventDate;
    public String eventType;
    public String description;
}
