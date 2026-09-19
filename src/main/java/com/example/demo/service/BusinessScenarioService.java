package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.entity.BusinessScenario;
import com.example.demo.entity.BusinessScenarioId;
import com.example.demo.repo.BusinessScenarioRepository;

@Service
public class BusinessScenarioService {

    @Autowired
    private BusinessScenarioRepository scenarioRepository;

    @Autowired
    private com.example.demo.repo.ScenarioActivitiesRepository activitiesRepository;

    // --- GET ALL ---
    public List<BusinessScenario> getAllScenarios() {
        return scenarioRepository.findAll();
    }

    // --- GET BY COMPANY (with pagination) ---
    public Page<BusinessScenario> getScenariosByCompany(String companyCode, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return scenarioRepository.findByIdCompanyCode(companyCode, pageable);
    }
    
    // --- GET BY COMPANY (without pagination - for backward compatibility) ---
    public List<BusinessScenario> getScenariosByCompany(String companyCode) {
        return scenarioRepository.findByIdCompanyCode(companyCode);
    }
    
    // --- SEARCH BY COMPANY AND KEYWORD (with pagination) ---
    public Page<BusinessScenario> searchScenariosByCompany(String companyCode, String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        if (keyword == null || keyword.trim().isEmpty()) {
            return scenarioRepository.findByIdCompanyCode(companyCode, pageable);
        }
        return scenarioRepository.searchByCompanyCodeAndKeyword(companyCode, keyword.trim(), pageable);
    }

    // --- GET BY ID ---
    public Optional<BusinessScenario> getScenarioById(BusinessScenarioId id) {
        return scenarioRepository.findById(id);
    }

    // --- CREATE / SAVE ---
    public BusinessScenario saveScenario(BusinessScenario scenario) {
        return scenarioRepository.save(scenario);
    }

    // --- UPDATE ---
    public BusinessScenario updateScenario(BusinessScenario scenario) {
        if (scenario.getId() == null) {
            throw new IllegalArgumentException("Scenario ID is required for update!");
        }
        Optional<BusinessScenario> existing = scenarioRepository.findById(scenario.getId());
        if (existing.isPresent()) {
            return scenarioRepository.save(scenario);
        } else {
            throw new IllegalArgumentException("Scenario not found for update!");
        }
    }

    // --- DELETE ---
    public void deleteScenario(String companyCode, String scenarioName) {
        BusinessScenarioId id = new BusinessScenarioId();
        id.setCompanyCode(companyCode);
        id.setBusinessScenario(scenarioName);
        scenarioRepository.deleteById(id);
    }

    public Optional<BusinessScenario> getScenarioByCompanyAndBusinessScenario(String companyCode, String businessScenario) {
        return scenarioRepository.findByIdCompanyCodeAndIdBusinessScenario(companyCode, businessScenario);
    }

    public boolean existsById(BusinessScenarioId id) {
        return scenarioRepository.existsById(id);
    }

    // Generate next business scenario number for a company
    public String generateNextBusinessScenario(String companyCode) {
        String lastScenario = scenarioRepository.findLastBusinessScenarioByCompany(companyCode);
        int nextNum = Integer.parseInt(lastScenario) + 1;
        return String.format("%02d", nextNum); // 2-digit format
    }

    public long getResponsibleCount(String companyCode) {
        return scenarioRepository.countDistinctResponsibleByCompany(companyCode);
    }

    public long getWorkStreamCount(String companyCode) {
        return scenarioRepository.countDistinctWorkStreamByCompany(companyCode);
    }

    public boolean hasChildren(String companyCode, String businessScenario) {
        return activitiesRepository.existsByIdBusinessScenarioIdCompanyCodeAndIdBusinessScenarioIdBusinessScenario(companyCode, businessScenario);
    }
}
