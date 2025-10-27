package com.example.demo.service;

import com.example.demo.dto.BusinessScenarioDTO;

import com.example.demo.entity.BusinessScenario;
import com.example.demo.repo.BusinessScenarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusinessScenarioService {

    @Autowired
    private BusinessScenarioRepository repo;

    public void saveScenario(BusinessScenarioDTO dto) {
        if (repo.existsByBusinessScenario(dto.getBusinessScenario())) {
            throw new IllegalArgumentException("Scenario with ID '" + dto.getBusinessScenario() + "' already exists.");
        }
        repo.save(toEntity(dto));
    }

    public List<BusinessScenarioDTO> getAllScenarios() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public BusinessScenarioDTO getScenarioDTOById(String id) {
        BusinessScenario entity = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Scenario not found with id: " + id));
        return toDTO(entity);
    }

    public void updateScenario(String id, BusinessScenarioDTO dto) {
        BusinessScenario existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Scenario not found with id: " + id));

        existing.setScenarioDescription(dto.getScenarioDescription());
        existing.setWorkStream(dto.getWorkStream());
        existing.setActivity(dto.getActivity());
        existing.setResponsible(dto.getResponsible());
        existing.setExpectedOutcome(dto.getExpectedOutcome());
        existing.setTcode(dto.getTcode());

        repo.save(existing);
    }

    private BusinessScenarioDTO toDTO(BusinessScenario entity) {
        BusinessScenarioDTO dto = new BusinessScenarioDTO();
        dto.setBusinessScenario(entity.getBusinessScenario());
        dto.setScenarioDescription(entity.getScenarioDescription());
        dto.setWorkStream(entity.getWorkStream());
        dto.setActivity(entity.getActivity());
        dto.setResponsible(entity.getResponsible());
        dto.setExpectedOutcome(entity.getExpectedOutcome());
        dto.setTcode(entity.getTcode());
        return dto;
    }

    private BusinessScenario toEntity(BusinessScenarioDTO dto) {
        BusinessScenario entity = new BusinessScenario();
        entity.setBusinessScenario(dto.getBusinessScenario());
        entity.setScenarioDescription(dto.getScenarioDescription());
        entity.setWorkStream(dto.getWorkStream());
        entity.setActivity(dto.getActivity());
        entity.setResponsible(dto.getResponsible());
        entity.setExpectedOutcome(dto.getExpectedOutcome());
        entity.setTcode(dto.getTcode());
        return entity;
    }
    
   
    public boolean existsByBusinessScenario(String businessScenario) {
        return repo.existsByBusinessScenario(businessScenario);
    }

}
