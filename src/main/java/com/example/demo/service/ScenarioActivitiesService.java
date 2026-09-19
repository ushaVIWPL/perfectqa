package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.embeddedid.ScenarioActivitiesId;
import com.example.demo.entity.ScenarioActivities;
import com.example.demo.repo.ScenarioActivitiesRepository;

@Service
public class ScenarioActivitiesService {

    @Autowired
    private ScenarioActivitiesRepository activitiesRepository;

    @Autowired
    private com.example.demo.repo.TestCaseHeaderRepository headerRepository;

    // Get all activities for a given business scenario + company (with pagination)
	/*
	 * public Page<ScenarioActivities> getActivitiesByScenario(String companyCode,
	 * String businessScenario, int page, int size, String sortBy, String sortDir) {
	 * Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() :
	 * Sort.by(sortBy).ascending(); Pageable pageable = PageRequest.of(page, size,
	 * sort); return activitiesRepository.
	 * findByIdBusinessScenarioIdCompanyCodeAndIdBusinessScenarioIdBusinessScenario(
	 * companyCode, businessScenario, pageable ); }
	 */
    
    public Page<ScenarioActivities> getActivitiesByScenario(
            String companyCode,
            String businessScenario,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ScenarioActivities> activities =
                activitiesRepository
                    .findByIdBusinessScenarioIdCompanyCodeAndIdBusinessScenarioIdBusinessScenario(
                        companyCode, businessScenario, pageable
                    );

        // ✅ ADD HERE (usage)
        activities.forEach(activity -> {
            String description =
                activity.getBusinessScenario().getScenarioDescription();
            // use it / map it / log it
        });

        return activities;
    }

    
    // Get all activities for a given business scenario + company (without pagination - for backward compatibility)
    public List<ScenarioActivities> getActivitiesByScenario(String companyCode, String businessScenario) {
        return activitiesRepository.findByIdBusinessScenarioIdCompanyCodeAndIdBusinessScenarioIdBusinessScenario(
                companyCode, businessScenario
        );
    }
    
    // Get all activities for a company (with pagination)
    public Page<ScenarioActivities> getActivitiesByCompanyCode(String companyCode, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return activitiesRepository.findByIdBusinessScenarioIdCompanyCode(companyCode, pageable);
    }
    
    // Search activities by company code and keyword (with pagination)
    public Page<ScenarioActivities> searchActivitiesByCompanyCode(String companyCode, String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        if (keyword == null || keyword.trim().isEmpty()) {
            return activitiesRepository.findByIdBusinessScenarioIdCompanyCode(companyCode, pageable);
        }
        return activitiesRepository.searchByCompanyCodeAndKeyword(companyCode, keyword.trim(), pageable);
    }

    // Get activity by composite ID
    public Optional<ScenarioActivities> getActivityById(ScenarioActivitiesId id) {
        return activitiesRepository.findById(id);
    }

    // Save or update activity
    public ScenarioActivities saveActivity(ScenarioActivities activity) {
        System.out.println("=== SERVICE: saveActivity called ===");
        
        // Ensure ID exists
        if (activity.getId() == null) {
            activity.setId(new ScenarioActivitiesId());
        }
        
        // CRITICAL: Ensure transactionSuffix is set before saving
        String suffix = activity.getId().getTransactionSuffix();
        System.out.println("Transaction Suffix before check: " + suffix);
        
        if (suffix == null || suffix.trim().isEmpty()) {
            String companyCode = activity.getId().getBusinessScenarioId() != null ? activity.getId().getBusinessScenarioId().getCompanyCode() : null;
            String businessScenario = activity.getId().getBusinessScenarioId() != null ? activity.getId().getBusinessScenarioId().getBusinessScenario() : null;
            if (companyCode != null && !companyCode.trim().isEmpty() && businessScenario != null && !businessScenario.trim().isEmpty()) {
                suffix = generateNextTransactionSuffix(companyCode, businessScenario);
                System.out.println("Auto-generating suffix in service: " + suffix);
            } else {
                suffix = "01";
                System.out.println("Falling back to suffix 01 in service");
            }
            activity.getId().setTransactionSuffix(suffix);
        }
        
        return activitiesRepository.save(activity);
    }

    // Delete activity by composite ID
    public void deleteActivity(ScenarioActivitiesId id) {
        activitiesRepository.deleteById(id);
    }
    public boolean activityExists(ScenarioActivitiesId id) {
        return activitiesRepository.existsById(id);
    }
    // Get all activities for a company (without pagination - for backward compatibility)


    // Find activity by transaction key
    public Optional<ScenarioActivities> getActivityByTransactionKey(String transactionKey) {
        if (transactionKey == null || transactionKey.trim().isEmpty()) {
            return Optional.empty();
        }
        return activitiesRepository.findByTransactionKey(transactionKey.trim());
    }

    // Generate next transaction suffix for a business scenario
    public String generateNextTransactionSuffix(String companyCode, String businessScenario) {
        String lastSuffix = activitiesRepository.findLastTransactionSuffix(companyCode, businessScenario);
        int nextNum = Integer.parseInt(lastSuffix) + 1;
        return String.format("%02d", nextNum); // 2-digit format
    }
    
    public List<ScenarioActivities> getAllActivities() {
        return activitiesRepository.findAll();
    }

    public List<ScenarioActivities> getActivitiesByCompanyCode(String companyCode) {
        return activitiesRepository.findByIdBusinessScenarioIdCompanyCode(companyCode.trim());
    }

    public long getDistinctScenariosCount(String companyCode) {
        return activitiesRepository.countDistinctBusinessScenariosByCompany(companyCode);
    }

    public long getDistinctTransactionKeysCount(String companyCode) {
        return activitiesRepository.countDistinctTransactionKeysByCompany(companyCode);
    }

    public boolean hasChildren(String transactionKey) {
        if (transactionKey == null || transactionKey.trim().isEmpty()) return false;
        return headerRepository.existsByTransactionKey(transactionKey.trim());
    }
}
