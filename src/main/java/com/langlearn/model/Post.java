package com.langlearn.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** A discussion-board message so learners can interact with each other. */
@Entity
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String authorName;
    public String authorRole;
    @Column(length = 1000)
    public String message;
    public LocalDateTime createdAt = LocalDateTime.now();
}
