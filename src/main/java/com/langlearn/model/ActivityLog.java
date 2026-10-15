package com.langlearn.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ActivityLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String message;
    public LocalDateTime time = LocalDateTime.now();

    public ActivityLog() {}
    public ActivityLog(String message) { this.message = message; }
}
