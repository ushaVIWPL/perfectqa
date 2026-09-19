package com.example.demo.entity;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name = "testcase_headers")
public class TestCaseHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_key", nullable = false)
    private String transactionKey;

    @Column(name = "test_case_no", nullable = false)
    private String testCaseNo;

    @Column(name = "company_code", nullable = false)
    private String companyCode;

    // ==================== RELATIONSHIP ======================
    @ManyToOne
    @JoinColumn(name = "transaction_key", referencedColumnName = "transaction_key", insertable = false, updatable = false)
    private ScenarioActivities scenarioActivity;

    // ======================= FIELDS =========================
    @Column(name = "combined_key", length = 20)
    private String combinedKey;     // e.g., 010101

    @Column(name = "activity")
    private String activity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "work_stream")
    private String workStream;

    @Column(name = "responsible")
    private String responsible;

    @Column(name = "expected_outcome", columnDefinition = "TEXT")
    private String expectedOutcome;

    @Column(name = "t_code")
    private String tCode;

    @Column(name = "prerequisites", columnDefinition = "TEXT")
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

    // =========== IMAGE FIELDS ============
    @Lob
    @Column(name = "screenshot")
    private byte[] screenshot;

    @Column(name = "screenshot_path")
    private String screenshotPath;

    @Column(name = "screenshot_paths", columnDefinition = "TEXT")
    private String screenshotPaths;

    @Transient
    private List<String> screenshotBase64List;

    @Transient
    private List<MultipartFile> files;
    

    public String getTransactionKey() { return transactionKey; }
    public void setTransactionKey(String transactionKey) { this.transactionKey = transactionKey; }


    // ===================== AUTO GENERATE COMBINED KEY ======================
	/*
	 * @PrePersist
	 * 
	 * @PreUpdate public void generateCombinedKey() { if (transactionKey != null &&
	 * testCaseNo != null) { this.combinedKey = transactionKey + testCaseNo; //
	 * e.g., "0101" + "01" = "010101" } }
	 */
    
    @PrePersist
    @PreUpdate
    public void beforeSaveOrUpdate() {

        // 1️⃣ Generate combined key
        if (transactionKey != null && testCaseNo != null) {
            this.combinedKey = transactionKey + testCaseNo;
        }

        // 2️⃣ Copy data from parent (ScenarioActivities → TestCaseHeader) ONLY if empty
        if (this.scenarioActivity != null) {
            if (this.activity == null || this.activity.isEmpty()) this.activity = scenarioActivity.getActivity();
            if (this.description == null || this.description.isEmpty()) this.description = scenarioActivity.getScenarioDescription();
            if (this.workStream == null || this.workStream.isEmpty()) this.workStream = scenarioActivity.getWorkStream();
            if (this.responsible == null || this.responsible.isEmpty()) this.responsible = scenarioActivity.getResponsible();
            if (this.expectedOutcome == null || this.expectedOutcome.isEmpty()) this.expectedOutcome = scenarioActivity.getExpectedOutcome();
            if (this.tCode == null || this.tCode.isEmpty()) this.tCode = scenarioActivity.getTcode();
        }
    }

    // Helper method to get image paths as a list of filenames
    public List<String> getImagePathsList() {
        List<String> paths = new java.util.ArrayList<>();
        if (screenshotPaths != null && !screenshotPaths.isEmpty()) {
            String[] split = screenshotPaths.split(",");
            for (String path : split) {
                if (path != null && !path.trim().isEmpty()) {
                    String trimmed = path.trim();
                    String filename = trimmed;
                    int lastSlashIdx = Math.max(trimmed.lastIndexOf('/'), trimmed.lastIndexOf('\\'));
                    if (lastSlashIdx >= 0) {
                        filename = trimmed.substring(lastSlashIdx + 1);
                    }
                    paths.add(filename);
                }
            }
        }
        return paths;
    }
}
