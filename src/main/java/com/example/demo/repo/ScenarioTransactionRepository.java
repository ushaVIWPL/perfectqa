package com.example.demo.repo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.BusinessScenario;
import com.example.demo.entity.Scenariotransaction;

public interface ScenarioTransactionRepository extends JpaRepository<Scenariotransaction, String> {

    List<Scenariotransaction> findByBusinessScenario(BusinessScenario businessScenario);

    void deleteByBusinessScenario(BusinessScenario businessScenario);
    Optional<Scenariotransaction> findByTransactionKey(String transactionKey);

    boolean existsByBusinessScenario(BusinessScenario businessScenario);

	Optional<Scenariotransaction> findByTransactionSuffix(String transactionSuffix);
	
	
	 @Query(value = """
	            SELECT
	              CASE WHEN B.row_num = 1 THEN A.business_scenario ELSE NULL END AS business_scenario,
	              CASE WHEN B.row_num = 1 THEN A.activity ELSE NULL END AS activity,
	              CASE WHEN B.row_num = 1 THEN A.expected_outcome ELSE NULL END AS expected_outcome,
	              CASE WHEN B.row_num = 1 THEN A.responsible ELSE NULL END AS responsible,
	              CASE WHEN B.row_num = 1 THEN A.scenario_description ELSE NULL END AS scenario_description,
	              CASE WHEN B.row_num = 1 THEN A.work_stream ELSE NULL END AS work_stream,
	              B.transaction_key,
	              B.activity AS trans_activity,
	              B.expected_outcome AS trans_expected_outcome,
	              B.responsible AS trans_responsible,
	              B.scenario_description AS trans_scenario_description,
	              B.tcode AS trans_tcode,
	              B.transaction_suffix,
	              B.work_stream AS trans_work_stream,
	              B.business_scenario
	            FROM (
	                SELECT *, ROW_NUMBER() OVER (PARTITION BY business_scenario ORDER BY transaction_key) AS row_num
	                FROM scenariotransaction
	            ) B
	            LEFT JOIN businessscenario A
	            ON A.business_scenario = B.business_scenario
	            """, nativeQuery = true)
	        List<Map<String, Object>> fetchScenarioTransactions();
	    }


