package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import com.example.demo.embeddedid.ScenarioActivitiesId;
import com.example.demo.listener.ScenarioActivitiesListener;

@Entity
@Table(name = "scenario_activities")
@EntityListeners(ScenarioActivitiesListener.class)
@Data
public class ScenarioActivities {

    @EmbeddedId
    private ScenarioActivitiesId id;

    @ManyToOne
    @MapsId("businessScenarioId")
    @JoinColumns({
        @JoinColumn(name = "business_scenario", referencedColumnName = "business_scenario"),
        @JoinColumn(name = "company_code", referencedColumnName = "company_code")
    })
    private BusinessScenario businessScenario;


    @Column(name = "transaction_key")
    private String transactionKey;

    @PrePersist
    @PreUpdate
    public void generateTransactionKey() {
        if (id == null) {
            id = new ScenarioActivitiesId();
        }
        
        // CRITICAL: Ensure transactionSuffix is ALWAYS set before persist
        String suffix = id.getTransactionSuffix(); // This will return "01" if null due to getter
        if (suffix == null || suffix.trim().isEmpty()) {
            id.setTransactionSuffix("01");
            suffix = "01";
        }
        
        // ONLY GENERATE IF NOT ALREADY SET (to protect existing data)
        if (this.transactionKey == null || this.transactionKey.trim().isEmpty()) {
            if (id.getBusinessScenarioId() != null) {
                String businessScenario = id.getBusinessScenarioId().getBusinessScenario();
                String companyCode = id.getBusinessScenarioId().getCompanyCode();
                
                if (businessScenario != null && suffix != null && companyCode != null) {
                    // Transaction key: company_code + "-" + business_scenario + transaction_suffix 
                    this.transactionKey = companyCode + "-" + businessScenario + suffix;
                }
            }
        }
        
        // Final check - ensure it's set in the ID object
        if (id.getTransactionSuffix() == null || id.getTransactionSuffix().trim().isEmpty()) {
            id.setTransactionSuffix("01");
        }
    }

    private String expectedOutcome;
    private String activity;
    private String tcode;
    private String scenarioDescription;
    private String responsible;
    private String workStream;
}
