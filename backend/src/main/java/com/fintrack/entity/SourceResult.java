package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "source_results")
public class SourceResult extends BaseEntity {
    public Long checkId;
    public Long personId;
    @ManyToOne(optional = false)
    @JoinColumn(name = "source_id")
    public Source source;
    @Enumerated(EnumType.STRING)
    public SourceStatus status;
    @Enumerated(EnumType.STRING)
    public MatchType matchType;
    public int matches;
    public String summary;
    public LocalDateTime checkedAt;
}
