package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "persons")
public class Person extends BaseEntity {
    /** Persona ficticia de referencia del dashboard (Juan Pérez). */
    public static final String BASELINE_DOCUMENT = "1.000.000.001";

    public String fullName;
    public String document;
    public String documentType;
    public String city;
    public LocalDate birthDate;
    public String occupation;
    public String overallStatus;
    public LocalDateTime lastCheckedAt;
    public int runNumber;
}
