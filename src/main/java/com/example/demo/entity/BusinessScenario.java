package com.example.demo.entity;

import jakarta.persistence.*;

import lombok.Data;

@Data
@Entity
@Table(name = "business_scenario")
public class BusinessScenario {

    @EmbeddedId
    private BusinessScenarioId id;

    @ManyToOne
    @MapsId("companyCode")
    @JoinColumn(name = "company_code")
    private CompanyRef company;

    private String activity;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String expectedOutcome;

    private String responsible;
    private String scenarioDescription;
    private String tcode;
    private String workStream;

    
    public void setCompany(CompanyRef company) {
        this.company = company;
    }

    // Optional getter
    public CompanyRef getCompany() {
        return company;
    }
  
}
