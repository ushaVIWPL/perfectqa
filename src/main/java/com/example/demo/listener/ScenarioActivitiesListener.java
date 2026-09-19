package com.example.demo.listener;

import com.example.demo.entity.ScenarioActivities;
import com.example.demo.embeddedid.ScenarioActivitiesId;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class ScenarioActivitiesListener {
    
    @PrePersist
    @PreUpdate
    public void ensureTransactionSuffix(ScenarioActivities activity) {
        if (activity.getId() == null) {
            activity.setId(new ScenarioActivitiesId());
        }
        
        ScenarioActivitiesId id = activity.getId();
        
        // CRITICAL: Set transactionSuffix if null or empty
        if (id.getTransactionSuffix() == null || id.getTransactionSuffix().trim().isEmpty()) {
            id.setTransactionSuffix("01");
        }
    }
}

























