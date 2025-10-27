package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "LanguageRef", schema = "perfectqa") // ✅ use your schema name if needed
public class LanguageRef {

    @Id
    @Column(name = "LanguageCode")
    private String languageCode;

    @Column(name = "Language")
    private String language;

    // Getters & Setters
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
}
