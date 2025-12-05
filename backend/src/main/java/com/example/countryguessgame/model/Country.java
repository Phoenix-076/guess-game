package com.example.countryguessgame.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "countries")
public class Country {
    @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

private String name;
private String emoji;
private String hints;
private String difficulty;
    
    public Country() {}
    
    public Country(String name, String emoji, String hints) {
        this.name = name;
        this.emoji = emoji;
        this.hints = hints;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
    
public String getHints() {
    return hints;
}

public void setHints(String hints) {
    this.hints = hints;
}

public String getDifficulty() {
    return difficulty;
}

public void setDifficulty(String difficulty) {
    this.difficulty = difficulty;
}
}
