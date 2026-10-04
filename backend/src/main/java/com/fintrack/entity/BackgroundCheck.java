package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "background_checks")
public class BackgroundCheck extends BaseEntity {
    public Long personId;
    @ManyToOne(optional = false)
    @JoinColumn(name = "source_id")
    public Source source;
    public String recordType;
    public String reference;
    public String matchedName;
    @Enumerated(EnumType.STRING)
    public MatchType matchType;
    public LocalDate recordDate;
    public String status;
    public String detail;
}
