package com.example.demo.entity;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name = "test_case_transactions")
public class TestCaseTransaction {
  
	
    @Id
    private String transactionId; // combinedKey + "-" + testCaseNo

    @Column(name = "test_case_no", nullable = true)
    private String testCaseNo;

    @Column(name = "main_key", nullable = true)
    private String mainKey;

    // Foreign key relationship
    @ManyToOne
    @JoinColumn(
        name = "main_key", 
        referencedColumnName = "combined_key", 
        insertable = false, 
        updatable = false, 
        nullable = true
    )
    private TestCaseHeader testCaseHeader;

    @Column(nullable = true)
    private Integer serialNo;

    @Column(nullable = true)
    private String type; 

    @Column(nullable = false)
    private String action;

    @Column(nullable = true)
    private String url;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String expectedResults;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String actualResults;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = true)
    private LocalDate testedDate;

    @Column(nullable = true)
    private String passFail;

    @Column(nullable = true)
    private String defects;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String comments;

    private String screenshotPaths;

    @Transient
    private List<String> base64Screenshots;

    public List<String> getBase64Screenshots() {
        return base64Screenshots;
    }
    public void setBase64Screenshots(List<String> base64Screenshots) {
        this.base64Screenshots = base64Screenshots;
    }
    
    
		
	}
   

