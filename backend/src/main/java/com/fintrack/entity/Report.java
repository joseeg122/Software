package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report extends BaseEntity {
    public String code;
    public Long personId;
    public LocalDateTime generatedAt;
    public String generatedBy;
    public String checkStatus;
    /** Fotografía JSON del perfil en el momento de generar el reporte. */
    @JsonRawValue
    @Column(columnDefinition = "text")
    public String snapshot;
}
