package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;

import com.example.demo.entity.Scenariotransaction;
import com.example.demo.injections.ScenarioResultProjection;

public interface ReportsRepository extends JpaRepository<Scenariotransaction, String> {

	
	@Query(value = """
            WITH scenariotransaction_cte AS (
                SELECT *,ROW_NUMBER() OVER (PARTITION BY business_scenario ORDER BY transaction_key) AS row_num 
                FROM scenariotransaction
            ),
            test_case_header_cte as (
                SELECT *,ROW_NUMBER() OVER (PARTITION BY transaction_key ORDER BY combined_key) AS row_num1 
                FROM test_case_header
            )
            SELECT
            CASE WHEN B.row_num = 1 and C.row_num1 = 1 THEN A.business_scenario ELSE NULL END AS businessScenario,
            CASE WHEN B.row_num = 1 and C.row_num1 = 1 THEN A.activity ELSE NULL END AS activity,
            CASE WHEN B.row_num = 1 and C.row_num1 = 1 THEN A.expected_outcome ELSE NULL END AS expectedOutcome,
            CASE WHEN B.row_num = 1 and C.row_num1 = 1 THEN A.responsible ELSE NULL END AS responsible,
            CASE WHEN B.row_num = 1 and C.row_num1 = 1 THEN A.scenario_description ELSE NULL END AS scenarioDescription,
            CASE WHEN B.row_num = 1 and C.row_num1 = 1 THEN A.work_stream ELSE NULL END AS workStream,

            CASE WHEN C.row_num1 = 1 THEN B.transaction_key ELSE NULL END AS transactionKey,
            CASE WHEN C.row_num1 = 1 THEN B.activity ELSE NULL END AS transActivity,
            CASE WHEN C.row_num1 = 1 THEN B.transaction_key ELSE NULL END AS transTransactionKey,
            CASE WHEN C.row_num1 = 1 THEN B.expected_outcome ELSE NULL END AS transExpectedOutcome,
            CASE WHEN C.row_num1 = 1 THEN B.responsible ELSE NULL END AS transResponsible,
            CASE WHEN C.row_num1 = 1 THEN B.scenario_description ELSE NULL END AS transScenarioDescription,
            CASE WHEN C.row_num1 = 1 THEN B.tcode ELSE NULL END AS tcode,
            CASE WHEN C.row_num1 = 1 THEN B.transaction_suffix ELSE NULL END AS transactionSuffix,
            CASE WHEN C.row_num1 = 1 THEN B.work_stream ELSE NULL END AS transWorkStream,

            C.combined_key AS combinedKey,
            C.activity AS tcActivity,
            C.description,
            C.end_date AS endDate,
            C.expected_outcome AS tcExpectedOutcome,
            C.navigate_steps AS navigateSteps,
            C.prerequisites,
            C.responsible AS tcResponsible,
            C.scenario,
            C.screen_shotjpg AS screenShotjpg,
            C.start_date AS startDate,
            C.success_criteria AS successCriteria,
            C.test_data AS testData,
            C.tested_by AS testedBy,
            C.transaction_key AS tcTransactionKey,
            C.work_stream AS tcWorkStream
            FROM scenariotransaction_cte B
            INNER JOIN businessscenario A ON A.business_scenario = B.business_scenario
            INNER JOIN test_case_header_cte C ON C.transaction_key=B.transaction_key
            """, nativeQuery = true)
	List<ScenarioResultProjection> fetchScenarioReport();
	
	
	 @Procedure(procedureName = "SP_DetailedTestCaseTransaction")
	    List<Object[]> getDetailedTestCaseTransaction();

}
	
	
	
	

