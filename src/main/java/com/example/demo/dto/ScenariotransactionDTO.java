package com.example.demo.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class ScenariotransactionDTO {
    private String businessScenario;
    private String activity;
    private String expectedOutcome;
    private String responsible;
    private String scenarioDescription;
    private String workStream;

    // Transaction fields
    private String transactionKey;
    private String transActivity;
    private String transTransactionKey;
    private String transExpectedOutcome;
    private String transResponsible;
    private String transScenarioDescription;
    private String tcode;
    private String transactionSuffix;
    private String transWorkStream;

    // Test case header fields
    private String combinedKey;
    private String description;
    private LocalDate endDate;
    private LocalDate startDate;
    private String navigateSteps;
    private String prerequisites;
    private String scenario;
    private String screenShotJpg;
    private String successCriteria;
    private String testData;
    private String testedBy;

    // Additional fields for mapping from SQL
    private String tcActivity;
    private String tcExpectedOutcome;
    private String tcResponsible;
    private String tcTransactionKey;
    private String transWorkStream2;  // optional duplicate if needed
    // You can add other fields if more columns exist in your SQL
}
