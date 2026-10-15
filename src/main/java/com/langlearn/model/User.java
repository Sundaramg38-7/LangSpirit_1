package com.langlearn.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "app_users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;
    @Column(unique = true)
    public String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String password;
    public String role; // ADMIN, INSTRUCTOR, LEARNER

    public User() {}
    public User(String name, String email, String password, String role) {
        this.name = name; this.email = email; this.password = password; this.role = role;
    }
}
