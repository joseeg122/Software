package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "timeline_events")
public class TimelineEvent extends BaseEntity {
    public Long personId;
    public String eventType;
    public String description;
    public LocalDateTime occurredAt;
}
