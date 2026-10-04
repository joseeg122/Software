package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sanctions")
public class Sanction extends BaseEntity {
    public Long personId;
    @ManyToOne(optional = false)
    @JoinColumn(name = "source_id")
    public Source source;
    public String listType;
    public String matchedName;
    @Enumerated(EnumType.STRING)
    public MatchType matchType;
    public String program;
    public LocalDate listedAt;
    public String detail;
}
