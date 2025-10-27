package com.example.demo.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ScenarioDetailsDTO;
import com.example.demo.entity.TestCaseHeader;
import com.example.demo.entity.TestCaseTransaction;
import com.example.demo.repo.TestCaseHeaderRepository;
import com.example.demo.repo.TestCaseTransactionRepository;

import jakarta.transaction.Transactional;

@Service
public class TestCaseHeaderService {

    @Autowired
    private TestCaseHeaderRepository testCaseHeaderRepository;

    @Autowired
    private TestCaseTransactionRepository testCaseTransactionRepository;

    
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    
    
    public TestCaseHeaderService(TestCaseHeaderRepository repository) {
        this.testCaseTransactionRepository = testCaseTransactionRepository;
    }

    public TestCaseHeader saveTestCase(TestCaseHeader header, List<MultipartFile> files) throws IOException {
        // generate key
        header.generateCombinedKey();

        // save files to folder
        if (files != null && !files.isEmpty()) {
            List<String> filePaths = files.stream().map(file -> {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                File dest = new File(uploadDir, fileName);
                dest.getParentFile().mkdirs(); // create folder if not exists
                try {
                    file.transferTo(dest);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to save file: " + fileName, e);
                }
                return dest.getAbsolutePath(); // store path
            }).collect(Collectors.toList());

            // join as comma-separated string
            header.setScreenshotPaths(String.join(",", filePaths));
        }

        return testCaseTransactionRepository.save(header);
    }
    
    public List<TestCaseHeader> findAll() {
        return testCaseHeaderRepository.findAll();
    }
    
    // --- Header methods ---
    public void saveTestCaseHeader(TestCaseHeader testCaseHeader) {
        testCaseHeaderRepository.save(testCaseHeader);
    }

    public List<TestCaseHeader> getAllTestCaseHeaders() {
        return testCaseHeaderRepository.findAll();
    }

    public Optional<TestCaseHeader> findByScenario(String scenario) {
        return testCaseHeaderRepository.findByScenario(scenario);
    }

    // --- Transaction methods ---
    public List<TestCaseTransaction> getAllTransactions() {
        return testCaseTransactionRepository.findAll();
    }

    
    public List<TestCaseHeader> findAllByScenario(String scenario) {
        return testCaseHeaderRepository.findAllByScenario(scenario);
    }
    public void updateTransaction(TestCaseTransaction txn) {
        testCaseTransactionRepository.save(txn); // uses JpaRepository
    }
    
    public Optional<TestCaseHeader> findById(String id) {
        return testCaseHeaderRepository.findById(id);
    }
    
    @Transactional
    public TestCaseHeader updateByScenario(String scenario, TestCaseHeader updated) {
        TestCaseHeader existing = testCaseHeaderRepository.findByScenario(scenario)
            .orElseThrow(() -> new RuntimeException("Record not found with Scenario: " + scenario));

        // Update only editable fields
        existing.setDescription(updated.getDescription());
        existing.setWorkStream(updated.getWorkStream());
        existing.setActivity(updated.getActivity());
        existing.setResponsible(updated.getResponsible());
        existing.setExpectedOutcome(updated.getExpectedOutcome());
        existing.setTCode(updated.getTCode());
        existing.setPrerequisites(updated.getPrerequisites());
        existing.setNavigateSteps(updated.getNavigateSteps());
        existing.setTestData(updated.getTestData());
        existing.setSuccessCriteria(updated.getSuccessCriteria());
        existing.setTestedBy(updated.getTestedBy());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());

        return testCaseHeaderRepository.save(existing);
    }
    
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<ScenarioDetailsDTO> getAllScenariosAsDTOs() {
        String sql = """
            SELECT
                A.business_scenario,
                A.activity,
                A.expected_outcome,
                A.responsible,
                A.scenario_description,
                A.work_stream,

                B.transaction_key,
                B.activity AS transActivity,
                B.expected_outcome AS transExpectedOutcome,
                B.responsible AS transResponsible,
                B.scenario_description AS transScenarioDescription,
                B.tcode,
                B.transaction_suffix,
                B.work_stream AS transWorkStream,

                C.combined_key,
                C.activity AS testActivity,
                C.description,
                C.end_date,
                C.expected_outcome AS testExpectedOutcome,
                C.navigate_steps,
                C.prerequisites,
                C.responsible AS testResponsible,
                C.scenario,
                C.screen_shotjpg,
                C.start_date,
                C.success_criteria,
                C.test_data,
                C.tested_by,
                C.transaction_key AS testTransactionKey,
                C.work_stream AS testWorkStream

            FROM businessscenario A
            LEFT JOIN scenariotransaction B ON A.business_scenario = B.business_scenario
            LEFT JOIN test_case_header C ON B.transaction_key = C.transaction_key
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ScenarioDetailsDTO dto = new ScenarioDetailsDTO();
            // BusinessScenario A
            dto.setBusinessScenario(rs.getString("business_scenario"));
            dto.setActivity(rs.getString("activity"));
            dto.setExpectedOutcome(rs.getString("expected_outcome"));
            dto.setResponsible(rs.getString("responsible"));
            dto.setScenarioDescription(rs.getString("scenario_description"));
            dto.setWorkStream(rs.getString("work_stream"));

            // Scenariotransaction B
            dto.setTransactionKey(rs.getString("transaction_key"));
            dto.setTransActivity(rs.getString("transActivity"));
            dto.setTransExpectedOutcome(rs.getString("transExpectedOutcome"));
            dto.setTransResponsible(rs.getString("transResponsible"));
            dto.setTransScenarioDescription(rs.getString("transScenarioDescription"));
            dto.setTcode(rs.getString("tcode"));
            dto.setTransactionSuffix(rs.getString("transaction_suffix"));
            dto.setTransWorkStream(rs.getString("transWorkStream"));

            // TestCaseHeader C
            dto.setCombinedKey(rs.getString("combined_key"));
            dto.setTestActivity(rs.getString("testActivity"));
            dto.setDescription(rs.getString("description"));
            dto.setEndDate(rs.getString("end_date"));
            dto.setTestExpectedOutcome(rs.getString("testExpectedOutcome"));
            dto.setNavigateSteps(rs.getString("navigate_steps"));
            dto.setPrerequisites(rs.getString("prerequisites"));
            dto.setTestResponsible(rs.getString("testResponsible"));
            dto.setScenario(rs.getString("scenario"));
            dto.setScreenShotjpg(rs.getString("screen_shotjpg"));
            dto.setStartDate(rs.getString("start_date"));
            dto.setSuccessCriteria(rs.getString("success_criteria"));
            dto.setTestData(rs.getString("test_data"));
            dto.setTestedBy(rs.getString("tested_by"));
            dto.setTestTransactionKey(rs.getString("testTransactionKey"));
            dto.setTestWorkStream(rs.getString("testWorkStream"));

            return dto;
        });
    }
    
    public boolean deleteHeaderScreenshot(String combinedKey, String fileName) {
        Optional<TestCaseHeader> optional = testCaseHeaderRepository.findById(combinedKey);
        if (optional.isEmpty()) return false;

        TestCaseHeader header = optional.get();
        List<String> paths = header.getScreenshotList();

        if (!paths.contains(fileName)) {
            return false;
        }

        // remove file from list
        paths = new ArrayList<>(paths);
        paths.remove(fileName);

        // update DB (comma-separated string)
        header.setScreenshotPaths(String.join(",", paths));
        testCaseHeaderRepository.save(header);

        // delete from disk (if stored locally)
        try {
            Path filePath = Paths.get("uploads/testheaders").resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

  

        @Autowired
        private TestCaseHeaderRepository repository;

        public Optional<TestCaseHeader> getHeaderByCombinedKey(String combinedKey) {
            return repository.findById(combinedKey);
        }

        // <-- Add this
        public TestCaseHeader saveHeader(TestCaseHeader header) {
            return repository.save(header);
        }
}




