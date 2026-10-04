package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "credits")
public class Credit extends BaseEntity {
    public Long personId;
    @ManyToOne(optional = false)
    @JoinColumn(name = "bank_id")
    public Bank bank;
    @JsonIgnore
    public String number;
    public String product;
    public String type;
    public BigDecimal initialAmount;
    public BigDecimal balance;
    public BigDecimal installment;
    public BigDecimal rate;
    public int termMonths;
    public String status;
    public int daysPastDue;
    public boolean restructured;
    public LocalDate openedAt;
    public LocalDate closedAt;

    public String getMaskedNumber() {
        return Masking.mask(number);
    }
}
