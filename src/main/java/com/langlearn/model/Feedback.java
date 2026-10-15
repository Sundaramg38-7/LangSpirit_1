package com.langlearn.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Feedback {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long learnerId;
    public String learnerName;
    public String instructorName;
    @Column(length = 1000)
    public String message;
    public LocalDateTime createdAt = LocalDateTime.now();
}
