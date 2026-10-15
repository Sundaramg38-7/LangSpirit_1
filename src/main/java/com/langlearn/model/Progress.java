package com.langlearn.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Progress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long learnerId;
    public String learnerName;
    public Long lessonId;
    public String lessonTitle;
    public int score;
    public int total;
    public LocalDateTime takenAt = LocalDateTime.now();
}
