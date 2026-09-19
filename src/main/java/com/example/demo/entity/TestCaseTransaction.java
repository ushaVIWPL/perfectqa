package com.example.demo.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "testcase_transactions")
public class TestCaseTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "testcase_header_id", nullable = false)
    private Long testcaseHeaderId;

    @Column(name = "step_no", nullable = false)
    private String stepNo;

    @Column(name = "company_code", nullable = false)
    private String companyCode;

    @Transient
    private String combinedKey; // Used for generating mainKey

    private String action;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String defects;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String comments;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String expectedResults;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String actualResults;


    private String passFail;
    @Column(name = "screenshot_paths", columnDefinition = "TEXT")
    private String screenshotPaths;
    private Integer serialNo;
    @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate testedDate;
    private String type;
    private String url;

    @Column(name = "main_key")
    private String mainKey;
  
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "testcase_header_id",
        referencedColumnName = "id",
        insertable = false,
        updatable = false
    )
    private TestCaseHeader testcaseHeader;
    @PrePersist
    @PreUpdate
    public void generateMainKey() {
        // Generate mainKey if combinedKey is set
        if (combinedKey != null && !combinedKey.isEmpty() && stepNo != null && !stepNo.isEmpty()) {
            this.mainKey = combinedKey + stepNo;
        }
    }

    // Helper method to get image paths as a list
    public List<String> getImagePathsList() {
        List<String> paths = new ArrayList<>();
        if (screenshotPaths != null && !screenshotPaths.isEmpty()) {
            String[] split = screenshotPaths.split(",");
            for (String path : split) {
                if (path != null && !path.trim().isEmpty()) {
                    String trimmed = path.trim();
                    // Extract only the filename from the path
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
