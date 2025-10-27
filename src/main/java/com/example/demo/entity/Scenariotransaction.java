package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "scenariotransaction")
public class Scenariotransaction {

    @Id
    @Column(name = "transaction_key", updatable = false, nullable = false, length = 50)
    private String transactionKey;

    @Column(name = "transaction_suffix", length = 50, nullable = false)
    private String transactionSuffix;

    @ManyToOne
    @JoinColumn(name = "business_scenario", referencedColumnName = "business_scenario", nullable = false)
    private BusinessScenario businessScenario;

    @Column(name = "scenario_description", length = 200, nullable = true)
    private String scenarioDescription;

    @Column(name = "work_stream", length = 5000, nullable = true)
    private String workStream;

    @Column(length = 200, nullable = true)
    private String activity;

    @Column(length = 200, nullable = true)
    private String responsible;

    @Column(name = "expected_outcome", length = 200, nullable = true)
    private String expectedOutcome;

    @Column(length = 200, nullable = true)
    private String tcode;

}
