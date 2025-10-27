package com.example.demo.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ScenariotransactionDTO;
import com.example.demo.entity.BusinessScenario;
import com.example.demo.entity.Scenariotransaction;
import com.example.demo.repo.BusinessScenarioRepository;
import com.example.demo.repo.ScenarioTransactionRepository;

import jakarta.transaction.Transactional;

@Service
public class ScenarioTransactionService {

    @Autowired
    private ScenarioTransactionRepository transactionRepository;

    @Autowired
    private BusinessScenarioRepository scenarioRepository;

    public void addTransactions(String businessScenarioId, List<ScenariotransactionDTO> dtoList) {
        BusinessScenario scenario = scenarioRepository.findById(businessScenarioId)
                .orElseThrow(() -> new RuntimeException("BusinessScenario not found: " + businessScenarioId));

        for (ScenariotransactionDTO dto : dtoList) {
            Scenariotransaction transaction = new Scenariotransaction();
            transaction.setBusinessScenario(scenario);
            transaction.setScenarioDescription(dto.getScenarioDescription());
            transaction.setWorkStream(dto.getWorkStream());
            transaction.setActivity(dto.getActivity());
            transaction.setResponsible(dto.getResponsible());
            transaction.setExpectedOutcome(dto.getExpectedOutcome());
            transaction.setTcode(dto.getTcode());
            transaction.setTransactionSuffix(dto.getTransactionSuffix());
            transactionRepository.save(transaction);
        }
    }

    public List<ScenariotransactionDTO> getTransactionsByBusinessScenario(String businessScenarioId) {
        BusinessScenario scenario = scenarioRepository.findById(businessScenarioId)
                .orElseThrow(() -> new RuntimeException("BusinessScenario not found: " + businessScenarioId));

        return transactionRepository.findByBusinessScenario(scenario).stream().map(tx -> {
            ScenariotransactionDTO dto = new ScenariotransactionDTO();
            dto.setTransactionKey(tx.getTransactionKey());
            dto.setScenarioDescription(tx.getScenarioDescription());
            dto.setWorkStream(tx.getWorkStream());
            dto.setActivity(tx.getActivity());
            dto.setResponsible(tx.getResponsible());
            dto.setExpectedOutcome(tx.getExpectedOutcome());
            dto.setTcode(tx.getTcode());
            return dto;
        }).collect(Collectors.toList());
    }

    public void deleteTransactionsByScenario(String businessScenarioId) {
        BusinessScenario scenario = scenarioRepository.findById(businessScenarioId)
                .orElseThrow(() -> new RuntimeException("BusinessScenario not found: " + businessScenarioId));
        transactionRepository.deleteByBusinessScenario(scenario);
    }

    public boolean scenarioHasTransactions(String businessScenarioId) {
        return scenarioRepository.findById(businessScenarioId)
                .map(transactionRepository::existsByBusinessScenario)
                .orElse(false);
    }

    // ⬇️ Renamed this for clarity
    public List<Scenariotransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    // ⬇️ Also renamed this for clarity
    public List<ScenariotransactionDTO> getAllTransactionDTOs() {
        return transactionRepository.findAll().stream().map(entity -> {
            ScenariotransactionDTO dto = new ScenariotransactionDTO();

            dto.setBusinessScenario(
                entity.getBusinessScenario() != null ? entity.getBusinessScenario().getBusinessScenario() : ""
            );
            dto.setTransactionKey(entity.getTransactionKey());
            dto.setTransactionSuffix(entity.getTransactionKey() != null && entity.getTransactionKey().length() > 2
                    ? entity.getTransactionKey().substring(2)
                    : "");
            dto.setScenarioDescription(entity.getScenarioDescription());
            dto.setWorkStream(entity.getWorkStream());
            dto.setActivity(entity.getActivity());
            dto.setResponsible(entity.getResponsible());
            dto.setExpectedOutcome(entity.getExpectedOutcome());
            dto.setTcode(entity.getTcode());

            return dto;
        }).collect(Collectors.toList());
    }
    
    
    public List<ScenariotransactionDTO> getAllScenariosAsDTOs() {
        return transactionRepository.findAll().stream().map(entity -> {
            ScenariotransactionDTO dto = new ScenariotransactionDTO();

            dto.setBusinessScenario(
                entity.getBusinessScenario() != null ? entity.getBusinessScenario().getBusinessScenario() : ""
            );
            dto.setTransactionKey(entity.getTransactionKey());
            dto.setTransactionSuffix(entity.getTransactionKey() != null && entity.getTransactionKey().length() > 2
                    ? entity.getTransactionKey().substring(2)
                    : "");
            dto.setScenarioDescription(entity.getScenarioDescription());
            dto.setWorkStream(entity.getWorkStream());
            dto.setActivity(entity.getActivity());
            dto.setResponsible(entity.getResponsible());
            dto.setExpectedOutcome(entity.getExpectedOutcome());
            dto.setTcode(entity.getTcode());

            return dto;
        }).collect(Collectors.toList());
    }
    public List<ScenariotransactionDTO> getAllScenarios() {
        return getAllScenariosAsDTOs(); // or implement differently
    }
    

    
    public List<Map<String, Object>> getAllScenarioTransactions() {
        return transactionRepository.fetchScenarioTransactions();
    }
    
    
    public Optional<Scenariotransaction> updateTransaction(String transactionKey, Scenariotransaction updatedData) {
        return transactionRepository.findById(transactionKey).map(existing -> {
            existing.setTransactionSuffix(updatedData.getTransactionSuffix());
            existing.setScenarioDescription(updatedData.getScenarioDescription());
            existing.setWorkStream(updatedData.getWorkStream());
            existing.setActivity(updatedData.getActivity());
            existing.setResponsible(updatedData.getResponsible());
            existing.setExpectedOutcome(updatedData.getExpectedOutcome());
            existing.setTcode(updatedData.getTcode());
            existing.setBusinessScenario(updatedData.getBusinessScenario());

            return transactionRepository.save(existing);
        });
    }
}


