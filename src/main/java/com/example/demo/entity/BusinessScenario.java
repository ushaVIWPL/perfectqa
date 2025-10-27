package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Data
@Entity
@Table(name = "businessscenario")
public class BusinessScenario {

    @Id
    @Column(name = "business_scenario")

    private String businessScenario;  // Primary key

    @Column(length = 5000, nullable = false)
    private String scenarioDescription;

    @Column(length = 200, nullable = true)
    private String workStream;

    @Column(length = 200, nullable = true)
    private String activity;

    @Column(length = 200, nullable = true)
    private String responsible;

    @Column(length = 5000, nullable = true)
    private String expectedOutcome;

    @Column(length = 15, nullable = true)
    private String tcode;

    // Getters and Setters

  
}
