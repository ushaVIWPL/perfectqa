package com.example.demo.dto;

import java.util.List;

import lombok.Data;

@Data

public class ScenarioDetailsDTO {




    // ---- Business Scenario (from A) ----
    private String businessScenario;
    private String activity;
    private String expectedOutcome;
    private String responsible;
    private String scenarioDescription;
    private String workStream;

    // ---- Scenario Transaction (from B) ----
    private String transactionKey;
    private String transActivity;
    private String transTransactionKey;
    private String transExpectedOutcome;
    private String transResponsible;
    private String transScenarioDescription;
    private String tcode;
    private String transactionSuffix;
    private String transWorkStream;

    // ---- Test Case Header (from C) ----
    private String combinedKey;
    private String testActivity;
    private String description;
    private String endDate;
    private String testExpectedOutcome;
    private String navigateSteps;
    private String prerequisites;
    private String testResponsible;
    private String scenario;
    private String screenShotjpg;
    private String startDate;
    private String successCriteria;
    private String testData;
    private String testedBy;
    private String testTransactionKey;
    private String testWorkStream;
    private List<ScenarioDetailsDTO> childTransactions; // must exist if used in template

}
