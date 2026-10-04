package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sources")
public class Source extends BaseEntity {
    public String code;
    public String name;
    public String category;
    /** Estado que devuelve la fuente cuando responde y no hay registros. */
    @Enumerated(EnumType.STRING)
    public SourceStatus emptyStatus;
    public String city;
    public Long bankId;
}
