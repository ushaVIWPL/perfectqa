package com.example.demo.dto;

import lombok.Data;

@Data
public class TestCaseHeaderDTO {

    private String transactionKey;       // FK to scenariotransaction
    private String combinedKey;          // from your SQL
    private String activity;
    private String description;
    private String endDate;
    private String expectedOutcome;
    private String navigateSteps;
    private String prerequisites;
    private String responsible;
    private String scenario;
    private String screenShotjpg;
    private String startDate;
    private String successCriteria;
    private String testData;
    private String testedBy;
    private String workStream;
}
