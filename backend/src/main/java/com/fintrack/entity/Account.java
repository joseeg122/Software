package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {
    public Long personId;
    @ManyToOne(optional = false)
    @JoinColumn(name = "bank_id")
    public Bank bank;
    /** Nunca se serializa completo: solo sale enmascarado. */
    @JsonIgnore
    public String number;
    public String type;
    public String status;
    public BigDecimal balance;
    public LocalDate openedAt;

    public String getMaskedNumber() {
        return Masking.mask(number);
    }
}
