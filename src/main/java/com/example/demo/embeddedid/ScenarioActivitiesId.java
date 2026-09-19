package com.example.demo.embeddedid;


import java.io.Serializable;

import com.example.demo.entity.BusinessScenarioId;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
@Access(AccessType.PROPERTY) // Force property access to use our getters/setters
public class ScenarioActivitiesId implements Serializable {

    @Column(name = "transaction_suffix", nullable = false)
    private String transactionSuffix;
    
    public String getTransactionSuffix() {
        return transactionSuffix;
    }
    
    public void setTransactionSuffix(String transactionSuffix) {
        this.transactionSuffix = transactionSuffix != null ? transactionSuffix.trim() : null;
    }

    @AttributeOverrides({
        @AttributeOverride(name="businessScenario", column=@Column(name="business_scenario")),
        @AttributeOverride(name="companyCode", column=@Column(name="company_code"))
    })
    private BusinessScenarioId businessScenarioId;
}

