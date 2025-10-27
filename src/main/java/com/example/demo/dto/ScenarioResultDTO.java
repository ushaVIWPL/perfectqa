package com.example.demo.dto;

import lombok.Data;

@Data
public class ScenarioResultDTO {
    private String businessScenario;
    private String activity;
    private String expectedOutcome;
    private String responsible;
    private String scenarioDescription;
    private String workStream;

    private String transactionKey;
    private String transActivity;
    private String transTransactionKey;
    private String transExpectedOutcome;
    private String transResponsible;
    private String transScenarioDescription;
    private String tcode;
    private String transactionSuffix;
    private String transWorkStream;

    private String combinedKey;
    private String tcActivity;
    private String description;
    private String endDate;
    private String tcExpectedOutcome;
    private String navigateSteps;
    private String prerequisites;
    private String tcResponsible;
    private String scenario;
    private String screenShotjpg;
    private String startDate;
    private String successCriteria;
    private String testData;
    private String testedBy;
    private String tcTransactionKey;
    private String tcWorkStream;
    private String transactionId;   // combinedKey + "-" + testCaseNo
    private String testCaseNo;
    private String mainKey;
    private String serialNo;
    private String type;
    private String action;
    private String url;
    private String expectedResults;
    private String actualResults;
    private String testedDate;     // use String for DTO (format as yyyy-MM-dd)
    private String passFail;
    private String defects;
    private String comments;
    private String screenshotPaths;
}
