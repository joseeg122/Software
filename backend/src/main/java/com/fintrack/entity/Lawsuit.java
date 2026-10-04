package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lawsuits")
public class Lawsuit extends BaseEntity {
    public Long personId;
    public Long processId;
    public String lawsuitNumber;
    public String claimType;
    public String plaintiff;
    public String defendant;
    public BigDecimal amount;
    public String status;
    public LocalDate filedAt;
}
