package com.example.demo.dto;


import lombok.Data;
import java.util.List;

@Data
public class TransactionWrapperDTO {
    private String businessScenario; // The ID of the scenario selected from dropdown
    private List<ScenariotransactionDTO> transactions; // List of transaction forms
}


