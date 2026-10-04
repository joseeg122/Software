package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_scores")
public class CreditScore extends BaseEntity {
    public Long personId;
    public int score;
    public int maxScore;
    public LocalDateTime calculatedAt;
    public int paymentHistory;
    public int debtLevel;
    public int cardUtilization;
    public int creditAge;
    public int activeCredits;
    public int delinquency;
}
