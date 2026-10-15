package com.langlearn.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String questionText;
    public String option1;
    public String option2;
    public String option3;
    public String option4;
    // 1..4 - hidden from API responses so learners cannot see answers
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public int correctOption;
}
