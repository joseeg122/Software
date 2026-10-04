package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reputational_news")
public class ReputationalNews extends BaseEntity {
    public Long personId;
    @ManyToOne(optional = false)
    @JoinColumn(name = "source_id")
    public Source source;
    public String title;
    public LocalDate publishedAt;
    public String outlet;
    public String category;
    public String level;
    public String status;
    @Enumerated(EnumType.STRING)
    public MatchType matchType;
}
