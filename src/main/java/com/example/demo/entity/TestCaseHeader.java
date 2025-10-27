package com.example.demo.entity;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name = "test_case_header")
public class TestCaseHeader {

    @Id
    @Column(name = "combined_key", nullable = false)
    private String combinedKey;

    
    @Lob
    @Column(name = "screenshot", columnDefinition = "LONGBLOB")
    private byte[] screenshot;

    
    private String screenshotPath; // single path
    @Transient
    private List<String> screenshotBase64List; // for Thymeleaf

    
    
    public List<String> getScreenshotBase64List() {
        return screenshotBase64List;
    }

    public void setScreenshotBase64List(List<String> screenshotBase64List) {
        this.screenshotBase64List = screenshotBase64List;
    }
    private String scenario;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "work_stream")
    private String workStream;

    private String activity;
    private String responsible;

    @Column(name = "expected_outcome", columnDefinition = "TEXT")
    private String expectedOutcome;

    @Column(name = "t_code")
    private String tCode;

    @Column(columnDefinition = "TEXT")
    private String prerequisites;

    @Column(name = "navigate_steps", columnDefinition = "TEXT")
    private String navigateSteps;

    @Column(name = "test_data", columnDefinition = "TEXT")
    private String testData;

    @Column(name = "success_criteria", columnDefinition = "TEXT")
    private String successCriteria;

    @Column(name = "tested_by")
    private String testedBy;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "start_date")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "transaction_key")
    private String transactionKey;

    // ✅ store multiple filenames (comma-separated)
    @Column(name = "screenshot_paths", columnDefinition = "TEXT")
    private String screenshotPaths;

    // transient fields (not persisted in DB)
    @Transient
    private List<MultipartFile> files;

    public void generateCombinedKey() {
        if (scenario != null && transactionKey != null) {
            this.combinedKey =  transactionKey + scenario;
        }
    }

    // helper to get screenshot list from DB string
    public List<String> getScreenshotList() {
        if (screenshotPaths == null || screenshotPaths.isBlank()) {
            return List.of();
        }
        return List.of(screenshotPaths.split(","));
    }
    
    
 // In TestCaseHeader.java
    public void setScreenshotList(List<String> screenshotList) {
        if (screenshotList == null || screenshotList.isEmpty()) {
            this.screenshotPaths = "";
        } else {
            this.screenshotPaths = String.join(",", screenshotList);
        }
    }

    
}


