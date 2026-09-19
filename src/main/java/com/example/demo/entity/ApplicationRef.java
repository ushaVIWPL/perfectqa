package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ApplicationRef {

    @Id
    private String appCode;

    private String appName;
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "project_code")
    private ProjectRef project;

    // --- Getters and Setters ---

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectRef getProject() {
        return project;
    }

    public void setProject(ProjectRef project) {
        this.project = project;
    }

	
	
}
