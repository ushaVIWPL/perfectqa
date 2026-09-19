package com.example.demo.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
@Embeddable
@Data
public class BusinessScenarioId implements Serializable {

    @Column(name = "business_scenario")
    private String businessScenario;

    @Column(name = "company_code")
    private String companyCode;
}


