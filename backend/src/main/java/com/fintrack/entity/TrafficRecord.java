package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "traffic_records")
public class TrafficRecord extends BaseEntity {
    public Long personId;
    @ManyToOne(optional = false)
    @JoinColumn(name = "source_id")
    public Source source;
    public String recordType;
    public String reference;
    public String city;
    public LocalDate recordDate;
    public BigDecimal amount;
    public String status;
    @Enumerated(EnumType.STRING)
    public MatchType matchType;
}
