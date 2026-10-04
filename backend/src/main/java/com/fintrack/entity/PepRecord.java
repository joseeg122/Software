package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pep_records")
public class PepRecord extends BaseEntity {
    public Long personId;
    @ManyToOne(optional = false)
    @JoinColumn(name = "source_id")
    public Source source;
    public String matchedName;
    public String position;
    public String entity;
    @Enumerated(EnumType.STRING)
    public MatchType matchType;
    public LocalDate fromDate;
    public LocalDate toDate;
    public String detail;
}
