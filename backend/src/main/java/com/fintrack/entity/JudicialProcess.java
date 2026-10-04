package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "judicial_processes")
public class JudicialProcess extends BaseEntity {
    public Long personId;
    @ManyToOne(optional = false)
    @JoinColumn(name = "source_id")
    public Source source;
    public String processNumber;
    public String processType;
    public String court;
    public String city;
    public String plaintiff;
    public String defendant;
    public String status;
    public String lastAction;
    public LocalDate lastActionDate;
    public LocalDate filedAt;
    @Enumerated(EnumType.STRING)
    public MatchType matchType;
    public boolean legalCollection;
}
