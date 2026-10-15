package com.langlearn.model;

import jakarta.persistence.*;

@Entity
public class Setting {
    @Id
    public String name;
    public String val;

    public Setting() {}
    public Setting(String name, String val) { this.name = name; this.val = val; }
}
