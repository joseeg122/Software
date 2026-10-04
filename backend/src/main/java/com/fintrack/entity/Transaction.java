package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {
    public Long accountId;
    public Long personId;
    public LocalDateTime txDate;
    public String description;
    public String type;
    public String channel;
    public BigDecimal amount;
    public BigDecimal balanceBefore;
    public BigDecimal balanceAfter;
}
