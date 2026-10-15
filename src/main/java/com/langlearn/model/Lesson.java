package com.langlearn.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Lesson {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String title;
    public String language;
    @Column(length = 5000)
    public String content;
    public String status; // PENDING, APPROVED, REJECTED
    public Long instructorId;
    public String instructorName;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "lesson_id")
    public List<Question> questions = new ArrayList<>();
}
