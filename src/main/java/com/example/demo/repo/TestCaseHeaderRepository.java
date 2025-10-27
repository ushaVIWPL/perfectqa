package com.example.demo.repo;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.TestCaseHeader;

public interface TestCaseHeaderRepository extends JpaRepository<TestCaseHeader, String> {
    Optional<TestCaseHeader> findByScenario(String scenario);
    Optional<TestCaseHeader> findByCombinedKey(String combinedKey);
    Optional<TestCaseHeader> findById(String id); // This is already inherited from JpaRepository
    List<TestCaseHeader> findAllByScenario(String scenario);


    
    
}
