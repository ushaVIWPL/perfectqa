package com.example.demo.dto;

import java.util.List;


import lombok.Data;

@Data
public class BusinessScenarioDTO {
    private String businessScenario;
    private String scenarioDescription;
    private String workStream;
    private String activity;
    private String responsible;
    private String expectedOutcome;
    private String tcode;
    private List<ScenariotransactionDTO> transactions;
}
