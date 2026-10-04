package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_payments")
public class CreditPayment extends BaseEntity {
    public Long creditId;
    public Long personId;
    public int installmentNo;
    public LocalDate dueDate;
    public LocalDate paidDate;
    public BigDecimal amount;
    public String status;
    public int daysLate;
}
