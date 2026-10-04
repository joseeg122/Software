package com.fintrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class AppUser extends BaseEntity {
    public String username;
    @JsonIgnore
    public String passwordHash;
    public String fullName;
    public String role;
    public LocalDateTime createdAt;
}
