package com.example.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.BusinessScenario;

public interface BusinessScenarioRepository extends JpaRepository<BusinessScenario, String> {
	boolean existsByBusinessScenario(String businessScenario);


}
