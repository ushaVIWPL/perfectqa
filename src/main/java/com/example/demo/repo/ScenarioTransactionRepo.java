package com.example.demo.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.ScenariotransactionDTO;

@Repository
public class ScenarioTransactionRepo {

    private final JdbcTemplate jdbcTemplate;

    public ScenarioTransactionRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ScenariotransactionDTO> getAllScenarioTransactions() {
        String sql = "WITH scenariotransaction_cte AS (" +
                     "SELECT *, ROW_NUMBER() OVER (PARTITION BY business_scenario ORDER BY transaction_key) AS row_num " +
                     "FROM scenariotransaction), " +
                     "test_case_header_cte AS (" +
                     "SELECT *, ROW_NUMBER() OVER (PARTITION BY transaction_key ORDER BY combined_key) AS row_num1 " +
                     "FROM test_case_header) " +
                     "SELECT " +
                     "CASE WHEN B.row_num = 1 and C.row_num1 = 1 THEN A.business_scenario ELSE NULL END AS business_scenario, " +
                     "...(rest of your query here)... " +
                     "FROM scenariotransaction_cte B " +
                     "INNER JOIN businessscenario A ON A.business_scenario = B.business_scenario " +
                     "INNER JOIN test_case_header_cte C ON C.transaction_key = B.transaction_key";

        return jdbcTemplate.query(sql, new RowMapper<ScenariotransactionDTO>() {
            @Override
            public ScenariotransactionDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                ScenariotransactionDTO dto = new ScenariotransactionDTO();
                dto.setBusinessScenario(rs.getString("business_scenario"));
                dto.setActivity(rs.getString("activity"));
                dto.setExpectedOutcome(rs.getString("expected_outcome"));
                dto.setResponsible(rs.getString("responsible"));
                dto.setScenarioDescription(rs.getString("scenario_description"));
                dto.setWorkStream(rs.getString("work_stream"));
                dto.setTransactionKey(rs.getString("transaction_key"));
                dto.setTransActivity(rs.getString("trans_activity"));
                dto.setTransTransactionKey(rs.getString("trans_transaction_key"));
                dto.setTransExpectedOutcome(rs.getString("trans_expected_outcome"));
                dto.setTransResponsible(rs.getString("trans_responsible"));
                dto.setTransScenarioDescription(rs.getString("trans_scenario_description"));
                dto.setTcode(rs.getString("tcode"));
                dto.setTransactionSuffix(rs.getString("transaction_suffix"));
                dto.setTransWorkStream(rs.getString("trans_work_stream"));
                dto.setCombinedKey(rs.getString("combined_key"));
                dto.setDescription(rs.getString("description"));
                dto.setEndDate(rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null);
                dto.setNavigateSteps(rs.getString("navigate_steps"));
                dto.setPrerequisites(rs.getString("prerequisites"));
                dto.setScenario(rs.getString("scenario"));
                dto.setScreenShotJpg(rs.getString("screen_shotjpg"));
                dto.setStartDate(rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null);
                dto.setSuccessCriteria(rs.getString("success_criteria"));
                dto.setTestData(rs.getString("test_data"));
                dto.setTestedBy(rs.getString("tested_by"));
                return dto;
            }
        });
    }
}
