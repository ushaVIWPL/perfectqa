package com.example.demo.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.BusinessScenario;
import com.example.demo.entity.ScenarioActivities;
import com.example.demo.entity.TestCaseHeader;
import com.example.demo.entity.TestCaseTransaction;
import com.example.demo.repo.BusinessScenarioRepository;
import com.example.demo.repo.ScenarioActivitiesRepository;
import com.example.demo.repo.TestCaseTransactionRepository;
import com.example.demo.repo.TicketTaskRepository;
import com.example.demo.entity.TicketTask;
import com.example.demo.service.ReportExportService;
import com.example.demo.service.TestCaseHeaderService;
import com.example.demo.service.UserAccountService;
import com.lowagie.text.DocumentException;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/api")
public class ReportsController {

    @Autowired
    private TestCaseHeaderService testCaseHeaderService;
    
    @Autowired
    private BusinessScenarioRepository businessScenarioRepository;
    
    @Autowired
    private TestCaseTransactionRepository testCaseTransactionRepository;
    
    @Autowired
    private ScenarioActivitiesRepository scenarioActivitiesRepository;
    
    @Autowired
    private TicketTaskRepository ticketTaskRepository;
    
    @Autowired
    private ReportExportService reportExportService;

    @Autowired
    private com.example.demo.repo.CompanyRepository companyRepository;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private com.example.demo.service.TicketService ticketService;

    @Autowired
    private com.example.demo.service.CompanyRefService companyRefService;

    private void addCompanyLogoToModel(Model model, HttpSession session, String companyCode) {
        String logoPath = null;
        if (session != null && session.getAttribute("companyLogo") != null) {
            logoPath = (String) session.getAttribute("companyLogo");
        }
        if (logoPath == null || logoPath.isEmpty()) {
            if (companyRefService != null && companyCode != null) {
                com.example.demo.entity.CompanyRef companyRef = companyRefService.findByCompanyCode(companyCode);
                if (companyRef != null && companyRef.getLogoPath() != null && !companyRef.getLogoPath().isEmpty()) {
                    logoPath = companyRef.getLogoPath();
                }
            }
        }
        if (logoPath == null || logoPath.isEmpty()) {
            if (companyRepository != null && companyCode != null) {
                com.example.demo.entity.Company company = companyRepository.findByCompanyCode(companyCode);
                if (company != null && company.getLogoUrl() != null && !company.getLogoUrl().isEmpty()) {
                    logoPath = company.getLogoUrl();
                }
            }
        }
        if (logoPath != null && !logoPath.isEmpty()) {
            model.addAttribute("companyLogoUrl", logoPath);
        }
    }

    /**
     * Test Case Headers Report - grouped by Business Scenario and Transaction Key
     */
    @GetMapping("/report-test-case-headers")
    public String getTestCaseHeadersReport(HttpSession session, Model model) {
        String companyCode = (String) session.getAttribute("companyCode");
        
        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "Company code not found in session. Please login again.");
            return "redirect:/loginform";
        }
        
        addCompanyLogoToModel(model, session, companyCode);
        
        // Get all test case headers for this company
        List<TestCaseHeader> headers = testCaseHeaderService.getByCompanyCode(companyCode);
        
        // Group by Business Scenario (from transaction key prefix) -> Transaction Key -> List of Headers
        // Transaction key format: businessScenario + suffix (e.g., "0101" where "01" is business scenario, "01" is suffix)
        Map<String, Map<String, List<TestCaseHeader>>> reportData = new LinkedHashMap<>();
        
        if (headers != null && !headers.isEmpty()) {
            // Group by transaction key first
            Map<String, List<TestCaseHeader>> groupedByTransactionKey = headers.stream()
                .filter(h -> h.getTransactionKey() != null)
                .collect(Collectors.groupingBy(
                    TestCaseHeader::getTransactionKey,
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
            
            // Now group by business scenario (first 2 chars of transaction key)
            for (Map.Entry<String, List<TestCaseHeader>> entry : groupedByTransactionKey.entrySet()) {
                String transactionKey = entry.getKey();
                List<TestCaseHeader> headerList = entry.getValue();
                
                // Extract business scenario from transaction key (first 2 characters)
                String businessScenario = transactionKey.length() >= 2 
                    ? transactionKey.substring(0, 2) 
                    : transactionKey;
                
                // Get or create the inner map for this business scenario
                reportData.computeIfAbsent(businessScenario, k -> new LinkedHashMap<>())
                    .put(transactionKey, headerList);
            }
        }
        
        model.addAttribute("companyCode", companyCode);
        model.addAttribute("reportData", reportData);
        
        return "report-test-case-headers";
    }
    
    /**
     * Export Test Case Headers to Excel
     */
    @GetMapping("/report-test-case-headers/excel")
    public ResponseEntity<ByteArrayResource> exportTestCaseHeadersExcel(HttpSession session) throws IOException {
        String companyCode = (String) session.getAttribute("companyCode");
        
        if (companyCode == null || companyCode.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Get all test case headers for this company
        List<TestCaseHeader> headers = testCaseHeaderService.getByCompanyCode(companyCode);
        
        // Group by Business Scenario -> Transaction Key -> List of Headers
        Map<String, Map<String, List<TestCaseHeader>>> reportData = new LinkedHashMap<>();
        
        if (headers != null && !headers.isEmpty()) {
            Map<String, List<TestCaseHeader>> groupedByTransactionKey = headers.stream()
                .filter(h -> h.getTransactionKey() != null)
                .collect(Collectors.groupingBy(
                    TestCaseHeader::getTransactionKey,
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
            
            for (Map.Entry<String, List<TestCaseHeader>> entry : groupedByTransactionKey.entrySet()) {
                String transactionKey = entry.getKey();
                List<TestCaseHeader> headerList = entry.getValue();
                
                String businessScenario = transactionKey.length() >= 2 
                    ? transactionKey.substring(0, 2) 
                    : transactionKey;
                
                reportData.computeIfAbsent(businessScenario, k -> new LinkedHashMap<>())
                    .put(transactionKey, headerList);
            }
        }
        
        byte[] excelData = reportExportService.exportTestCaseHeadersToExcel(reportData, companyCode);
        String fileName = "Test_Case_Headers_" + companyCode + "_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        
        ByteArrayResource resource = new ByteArrayResource(excelData);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .contentLength(excelData.length)
            .body(resource);
    }
    
    /**
     * Export Test Case Headers to PDF
     */
    @GetMapping("/report-test-case-headers/pdf")
    public ResponseEntity<ByteArrayResource> exportTestCaseHeadersPdf(HttpSession session) throws DocumentException {
        String companyCode = (String) session.getAttribute("companyCode");
        
        if (companyCode == null || companyCode.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Get all test case headers for this company
        List<TestCaseHeader> headers = testCaseHeaderService.getByCompanyCode(companyCode);
        
        // Group by Business Scenario -> Transaction Key -> List of Headers
        Map<String, Map<String, List<TestCaseHeader>>> reportData = new LinkedHashMap<>();
        
        if (headers != null && !headers.isEmpty()) {
            Map<String, List<TestCaseHeader>> groupedByTransactionKey = headers.stream()
                .filter(h -> h.getTransactionKey() != null)
                .collect(Collectors.groupingBy(
                    TestCaseHeader::getTransactionKey,
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
            
            for (Map.Entry<String, List<TestCaseHeader>> entry : groupedByTransactionKey.entrySet()) {
                String transactionKey = entry.getKey();
                List<TestCaseHeader> headerList = entry.getValue();
                
                String businessScenario = transactionKey.length() >= 2 
                    ? transactionKey.substring(0, 2) 
                    : transactionKey;
                
                reportData.computeIfAbsent(businessScenario, k -> new LinkedHashMap<>())
                    .put(transactionKey, headerList);
            }
        }
        
        byte[] pdfData = reportExportService.exportTestCaseHeadersToPDF(reportData, companyCode);
        String fileName = "Test_Case_Headers_" + companyCode + "_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        ByteArrayResource resource = new ByteArrayResource(pdfData);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfData.length)
            .body(resource);
    }
    
    // ==================== BUSINESS SCENARIOS REPORT ====================
    
    /**
     * Business Scenarios Report - List all business scenarios for a company
     */
    @GetMapping("/report-business-scenarios")
    public String getBusinessScenariosReport(HttpSession session, Model model) {
        String companyCode = (String) session.getAttribute("companyCode");
        
        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "Company code not found in session. Please login again.");
            return "redirect:/loginform";
        }
        
        addCompanyLogoToModel(model, session, companyCode);
        
        // Get all business scenarios for this company
        List<BusinessScenario> scenarios = businessScenarioRepository.findByIdCompanyCode(companyCode);
        
        model.addAttribute("companyCode", companyCode);
        model.addAttribute("scenarios", scenarios);
        model.addAttribute("totalScenarios", scenarios != null ? scenarios.size() : 0);
        
        return "report-business-scenarios";
    }
    
    /**
     * Export Business Scenarios to Excel
     */
    @GetMapping("/report-business-scenarios/excel")
    public ResponseEntity<ByteArrayResource> exportBusinessScenariosExcel(HttpSession session) throws IOException {
        String companyCode = (String) session.getAttribute("companyCode");
        
        if (companyCode == null || companyCode.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Get all business scenarios for this company
        List<BusinessScenario> scenarios = businessScenarioRepository.findByIdCompanyCode(companyCode);
        
        byte[] excelData = reportExportService.exportBusinessScenariosToExcel(scenarios, companyCode);
        String fileName = "Business_Scenarios_" + companyCode + "_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        
        ByteArrayResource resource = new ByteArrayResource(excelData);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .contentLength(excelData.length)
            .body(resource);
    }
    
    /**
     * Export Business Scenarios to PDF
     */
    @GetMapping("/report-business-scenarios/pdf")
    public ResponseEntity<ByteArrayResource> exportBusinessScenariosPdf(HttpSession session) throws DocumentException {
        String companyCode = (String) session.getAttribute("companyCode");
        
        if (companyCode == null || companyCode.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Get all business scenarios for this company
        List<BusinessScenario> scenarios = businessScenarioRepository.findByIdCompanyCode(companyCode);
        
        byte[] pdfData = reportExportService.exportBusinessScenariosToPDF(scenarios, companyCode);
        String fileName = "Business_Scenarios_" + companyCode + "_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        ByteArrayResource resource = new ByteArrayResource(pdfData);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfData.length)
            .body(resource);
    }
    
    // ==================== TRANSACTIONS REPORT ====================
    
    /**
     * Transactions Report - grouped by Business Scenario, Transaction Key, and Combined Key (Header)
     */
    @GetMapping("/report-transactions")
    public String getTransactionsReport(HttpSession session, Model model) {
        String companyCode = (String) session.getAttribute("companyCode");
        
        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "Company code not found in session. Please login again.");
            return "redirect:/loginform";
        }
        
        addCompanyLogoToModel(model, session, companyCode);
        
        // Get all transactions for this company
        List<TestCaseTransaction> transactions = testCaseTransactionRepository.findByCompanyCode(companyCode);
        
        // Get all test case headers for this company (to get combinedKey mapping)
        List<TestCaseHeader> headers = testCaseHeaderService.getByCompanyCode(companyCode);
        
        // Create a map of testcaseHeaderId to combinedKey and transactionKey
        Map<Long, String> headerIdToCombinedKey = new LinkedHashMap<>();
        Map<Long, String> headerIdToTransactionKey = new LinkedHashMap<>();
        for (TestCaseHeader header : headers) {
            headerIdToCombinedKey.put(header.getId(), header.getCombinedKey());
            headerIdToTransactionKey.put(header.getId(), header.getTransactionKey());
        }
        
        // Group transactions: Business Scenario -> Transaction Key -> Combined Key -> List of Transactions
        // Business Scenario = first 2 chars of transaction key
        // Transaction Key = first 4 chars of combined key (or from header)
        // Combined Key = first 6 chars of main key (or from header)
        Map<String, Map<String, Map<String, List<TestCaseTransaction>>>> reportData = new LinkedHashMap<>();
        
        if (transactions != null && !transactions.isEmpty()) {
            for (TestCaseTransaction txn : transactions) {
                String combinedKey = headerIdToCombinedKey.get(txn.getTestcaseHeaderId());
                String transactionKey = headerIdToTransactionKey.get(txn.getTestcaseHeaderId());
                
                // Fallback to parsing mainKey if header not found
                if (combinedKey == null && txn.getMainKey() != null && txn.getMainKey().length() >= 6) {
                    combinedKey = txn.getMainKey().substring(0, 6);
                }
                if (transactionKey == null && txn.getMainKey() != null && txn.getMainKey().length() >= 4) {
                    transactionKey = txn.getMainKey().substring(0, 4);
                }
                
                // Extract business scenario (first 2 chars of transaction key)
                String businessScenario = (transactionKey != null && transactionKey.length() >= 2) 
                    ? transactionKey.substring(0, 2) 
                    : "Unknown";
                
                // Use defaults if still null
                if (transactionKey == null) transactionKey = "Unknown";
                if (combinedKey == null) combinedKey = "Unknown";
                
                // Build nested structure
                reportData
                    .computeIfAbsent(businessScenario, k -> new LinkedHashMap<>())
                    .computeIfAbsent(transactionKey, k -> new LinkedHashMap<>())
                    .computeIfAbsent(combinedKey, k -> new java.util.ArrayList<>())
                    .add(txn);
            }
        }
        
        model.addAttribute("companyCode", companyCode);
        model.addAttribute("reportData", reportData);
        
        return "report-transactions";
    }
    
    /**
     * Export Transactions to Excel
     */
    @GetMapping("/report-transactions/excel")
    public ResponseEntity<ByteArrayResource> exportTransactionsExcel(HttpSession session) throws IOException {
        String companyCode = (String) session.getAttribute("companyCode");
        
        if (companyCode == null || companyCode.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Get all transactions for this company
        List<TestCaseTransaction> transactions = testCaseTransactionRepository.findByCompanyCode(companyCode);
        
        // Get all test case headers for this company (to get combinedKey mapping)
        List<TestCaseHeader> headers = testCaseHeaderService.getByCompanyCode(companyCode);
        
        // Create a map of testcaseHeaderId to combinedKey and transactionKey
        Map<Long, String> headerIdToCombinedKey = new LinkedHashMap<>();
        Map<Long, String> headerIdToTransactionKey = new LinkedHashMap<>();
        for (TestCaseHeader header : headers) {
            headerIdToCombinedKey.put(header.getId(), header.getCombinedKey());
            headerIdToTransactionKey.put(header.getId(), header.getTransactionKey());
        }
        
        // Group transactions: Business Scenario -> Transaction Key -> Combined Key -> List of Transactions
        Map<String, Map<String, Map<String, List<TestCaseTransaction>>>> reportData = new LinkedHashMap<>();
        
        if (transactions != null && !transactions.isEmpty()) {
            for (TestCaseTransaction txn : transactions) {
                String combinedKey = headerIdToCombinedKey.get(txn.getTestcaseHeaderId());
                String transactionKey = headerIdToTransactionKey.get(txn.getTestcaseHeaderId());
                
                // Fallback to parsing mainKey if header not found
                if (combinedKey == null && txn.getMainKey() != null && txn.getMainKey().length() >= 6) {
                    combinedKey = txn.getMainKey().substring(0, 6);
                }
                if (transactionKey == null && txn.getMainKey() != null && txn.getMainKey().length() >= 4) {
                    transactionKey = txn.getMainKey().substring(0, 4);
                }
                
                // Extract business scenario (first 2 chars of transaction key)
                String businessScenario = (transactionKey != null && transactionKey.length() >= 2) 
                    ? transactionKey.substring(0, 2) 
                    : "Unknown";
                
                // Use defaults if still null
                if (transactionKey == null) transactionKey = "Unknown";
                if (combinedKey == null) combinedKey = "Unknown";
                
                // Build nested structure
                reportData
                    .computeIfAbsent(businessScenario, k -> new LinkedHashMap<>())
                    .computeIfAbsent(transactionKey, k -> new LinkedHashMap<>())
                    .computeIfAbsent(combinedKey, k -> new java.util.ArrayList<>())
                    .add(txn);
            }
        }
        
        byte[] excelData = reportExportService.exportTransactionsToExcel(reportData, companyCode);
        String fileName = "Transactions_" + companyCode + "_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        
        ByteArrayResource resource = new ByteArrayResource(excelData);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .contentLength(excelData.length)
            .body(resource);
    }
    
    /**
     * Export Transactions to PDF
     */
    @GetMapping("/report-transactions/pdf")
    public ResponseEntity<ByteArrayResource> exportTransactionsPdf(HttpSession session) throws DocumentException {
        String companyCode = (String) session.getAttribute("companyCode");
        
        if (companyCode == null || companyCode.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Get all transactions for this company
        List<TestCaseTransaction> transactions = testCaseTransactionRepository.findByCompanyCode(companyCode);
        
        // Get all test case headers for this company (to get combinedKey mapping)
        List<TestCaseHeader> headers = testCaseHeaderService.getByCompanyCode(companyCode);
        
        // Create a map of testcaseHeaderId to combinedKey and transactionKey
        Map<Long, String> headerIdToCombinedKey = new LinkedHashMap<>();
        Map<Long, String> headerIdToTransactionKey = new LinkedHashMap<>();
        for (TestCaseHeader header : headers) {
            headerIdToCombinedKey.put(header.getId(), header.getCombinedKey());
            headerIdToTransactionKey.put(header.getId(), header.getTransactionKey());
        }
        
        // Group transactions: Business Scenario -> Transaction Key -> Combined Key -> List of Transactions
        Map<String, Map<String, Map<String, List<TestCaseTransaction>>>> reportData = new LinkedHashMap<>();
        
        if (transactions != null && !transactions.isEmpty()) {
            for (TestCaseTransaction txn : transactions) {
                String combinedKey = headerIdToCombinedKey.get(txn.getTestcaseHeaderId());
                String transactionKey = headerIdToTransactionKey.get(txn.getTestcaseHeaderId());
                
                // Fallback to parsing mainKey if header not found
                if (combinedKey == null && txn.getMainKey() != null && txn.getMainKey().length() >= 6) {
                    combinedKey = txn.getMainKey().substring(0, 6);
                }
                if (transactionKey == null && txn.getMainKey() != null && txn.getMainKey().length() >= 4) {
                    transactionKey = txn.getMainKey().substring(0, 4);
                }
                
                // Extract business scenario (first 2 chars of transaction key)
                String businessScenario = (transactionKey != null && transactionKey.length() >= 2) 
                    ? transactionKey.substring(0, 2) 
                    : "Unknown";
                
                // Use defaults if still null
                if (transactionKey == null) transactionKey = "Unknown";
                if (combinedKey == null) combinedKey = "Unknown";
                
                // Build nested structure
                reportData
                    .computeIfAbsent(businessScenario, k -> new LinkedHashMap<>())
                    .computeIfAbsent(transactionKey, k -> new LinkedHashMap<>())
                    .computeIfAbsent(combinedKey, k -> new java.util.ArrayList<>())
                    .add(txn);
            }
        }
        
        byte[] pdfData = reportExportService.exportTransactionsToPDF(reportData, companyCode);
        String fileName = "Transactions_" + companyCode + "_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        ByteArrayResource resource = new ByteArrayResource(pdfData);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfData.length)
            .body(resource);
    }
    
    // ==================== SCENARIO ACTIVITIES REPORT ====================
    
    /**
     * Scenario Activities Report - grouped by Business Scenario
     */
    @GetMapping("/scenario-activities")
    public String getScenarioActivitiesReport(HttpSession session, Model model) {
        String companyCode = (String) session.getAttribute("companyCode");
        
        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "Company code not found in session. Please login again.");
            return "redirect:/loginform";
        }
        
        addCompanyLogoToModel(model, session, companyCode);
        
        // Get all scenario activities for this company
        List<ScenarioActivities> activities = scenarioActivitiesRepository.findByIdBusinessScenarioIdCompanyCode(companyCode);
        
        // Group by Business Scenario
        Map<String, List<ScenarioActivities>> reportData = new LinkedHashMap<>();
        
        if (activities != null && !activities.isEmpty()) {
            reportData = activities.stream()
                .filter(a -> a.getId() != null && a.getId().getBusinessScenarioId() != null)
                .collect(Collectors.groupingBy(
                    a -> a.getId().getBusinessScenarioId().getBusinessScenario(),
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
        }
        
        // Get unique business scenarios count
        int totalScenarios = reportData.size();
        int totalActivities = activities != null ? activities.size() : 0;
        
        model.addAttribute("companyCode", companyCode);
        model.addAttribute("reportData", reportData);
        model.addAttribute("totalScenarios", totalScenarios);
        model.addAttribute("totalActivities", totalActivities);
        
        return "report-scenario-activities";
    }
    
    /**
     * Export Scenario Activities to Excel
     */
    @GetMapping("/scenario-activities/excel")
    public ResponseEntity<ByteArrayResource> exportScenarioActivitiesExcel(HttpSession session) throws IOException {
        String companyCode = (String) session.getAttribute("companyCode");
        
        if (companyCode == null || companyCode.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Get all scenario activities for this company
        List<ScenarioActivities> activities = scenarioActivitiesRepository.findByIdBusinessScenarioIdCompanyCode(companyCode);
        
        // Group by Business Scenario
        Map<String, List<ScenarioActivities>> reportData = new LinkedHashMap<>();
        
        if (activities != null && !activities.isEmpty()) {
            reportData = activities.stream()
                .filter(a -> a.getId() != null && a.getId().getBusinessScenarioId() != null)
                .collect(Collectors.groupingBy(
                    a -> a.getId().getBusinessScenarioId().getBusinessScenario(),
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
        }
        
        byte[] excelData = reportExportService.exportScenarioActivitiesToExcel(reportData, companyCode);
        String fileName = "Scenario_Activities_" + companyCode + "_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        
        ByteArrayResource resource = new ByteArrayResource(excelData);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .contentLength(excelData.length)
            .body(resource);
    }
    
    /**
     * Export Scenario Activities to PDF
     */
    @GetMapping("/scenario-activities/pdf")
    public ResponseEntity<ByteArrayResource> exportScenarioActivitiesPdf(HttpSession session) throws DocumentException {
        String companyCode = (String) session.getAttribute("companyCode");
        
        if (companyCode == null || companyCode.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Get all scenario activities for this company
        List<ScenarioActivities> activities = scenarioActivitiesRepository.findByIdBusinessScenarioIdCompanyCode(companyCode);
        
        // Group by Business Scenario
        Map<String, List<ScenarioActivities>> reportData = new LinkedHashMap<>();
        
        if (activities != null && !activities.isEmpty()) {
            reportData = activities.stream()
                .filter(a -> a.getId() != null && a.getId().getBusinessScenarioId() != null)
                .collect(Collectors.groupingBy(
                    a -> a.getId().getBusinessScenarioId().getBusinessScenario(),
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
        }
        
        byte[] pdfData = reportExportService.exportScenarioActivitiesToPDF(reportData, companyCode);
        String fileName = "Scenario_Activities_" + companyCode + "_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        ByteArrayResource resource = new ByteArrayResource(pdfData);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfData.length)
            .body(resource);
    }

    // ==================== BILLABLE TASK REPORT ====================

    @GetMapping("/report-billable-tasks")
    public String getBillableTaskReport(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String startDate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String endDate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String performedBy,
            HttpSession session, 
            Model model) {
        
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            return "redirect:/loginform";
        }
        model.addAttribute("companyCode", companyCode);
        
        addCompanyLogoToModel(model, session, companyCode);
        
        // Get all company users for the dropdown
        model.addAttribute("companyUsers", userAccountService.getUsersByCompanyCode(companyCode));

        if (startDate == null || startDate.trim().isEmpty()) {
            startDate = java.time.LocalDate.now().minusDays(30).toString();
        }
        if (endDate == null || endDate.trim().isEmpty()) {
            endDate = java.time.LocalDate.now().toString();
        }

        try {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            
            List<TicketTask> tasks = ticketTaskRepository.findByCompanyCodeAndDates(companyCode, start, end);
            
            String role = (String) session.getAttribute("role");
            String userId = (String) session.getAttribute("userId");
            String upperRole = (role != null) ? role.toUpperCase() : "GUEST";
            
            // If filtering by a specific user
            if (performedBy != null && !performedBy.trim().isEmpty()) {
                tasks = tasks.stream()
                        .filter(t -> performedBy.equalsIgnoreCase(t.getPerformedBy()))
                        .collect(Collectors.toList());
                model.addAttribute("performedBy", performedBy);
            } else if (!"ADMIN".equals(upperRole) && !"BUSINESS_MANAGER".equals(upperRole)) {
                // Regular users see only their own tasks if no specific filter (isolation)
                tasks = tasks.stream()
                        .filter(t -> userId != null && userId.equalsIgnoreCase(t.getPerformedBy()))
                        .collect(Collectors.toList());
            }
            
            double totalHours = tasks.stream()
                    .filter(t -> t.getBillableHours() != null)
                    .mapToDouble(TicketTask::getBillableHours)
                    .sum();
                    
            model.addAttribute("tasks", tasks);
            model.addAttribute("totalBillableHours", totalHours);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
        } catch (Exception e) {
            model.addAttribute("error", "Invalid date format.");
        }
        
        return "report-billable-tasks";
    }

    @GetMapping("/report-billable-tasks/excel")
    public ResponseEntity<ByteArrayResource> exportBillableTasksExcel(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String startDate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String endDate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String performedBy,
            HttpSession session) throws IOException {
        
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (startDate == null || startDate.trim().isEmpty()) {
            startDate = java.time.LocalDate.now().minusDays(30).toString();
        }
        if (endDate == null || endDate.trim().isEmpty()) {
            endDate = java.time.LocalDate.now().toString();
        }
        
        java.time.LocalDate start = java.time.LocalDate.parse(startDate);
        java.time.LocalDate end = java.time.LocalDate.parse(endDate);
        
        List<TicketTask> tasks = ticketTaskRepository.findByCompanyCodeAndDates(companyCode, start, end);
                
        String role = (String) session.getAttribute("role");
        String userId = (String) session.getAttribute("userId");
        String upperRole = (role != null) ? role.toUpperCase() : "GUEST";
        
        if (performedBy != null && !performedBy.trim().isEmpty()) {
            tasks = tasks.stream()
                    .filter(t -> performedBy.equalsIgnoreCase(t.getPerformedBy()))
                    .collect(Collectors.toList());
        } else if (!"ADMIN".equals(upperRole) && !"BUSINESS_MANAGER".equals(upperRole)) {
            tasks = tasks.stream()
                    .filter(t -> userId != null && userId.equalsIgnoreCase(t.getPerformedBy()))
                    .collect(Collectors.toList());
        }
                
        byte[] excelData = reportExportService.exportBillableTasksToExcel(tasks, companyCode, startDate, endDate, performedBy);
        String fileName = "Tasks_" + companyCode + "_" + startDate + "_to_" + endDate + ".xlsx";
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
            .header(HttpHeaders.PRAGMA, "no-cache")
            .header(HttpHeaders.EXPIRES, "0")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .contentLength(excelData.length)
            .body(new ByteArrayResource(excelData));
    }

    @GetMapping("/report-billable-tasks/pdf")
    public ResponseEntity<ByteArrayResource> exportBillableTasksPdf(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String startDate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String endDate,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String performedBy,
            HttpSession session) throws DocumentException {
        
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (startDate == null || startDate.trim().isEmpty()) {
            startDate = java.time.LocalDate.now().minusDays(30).toString();
        }
        if (endDate == null || endDate.trim().isEmpty()) {
            endDate = java.time.LocalDate.now().toString();
        }
        
        java.time.LocalDate start = java.time.LocalDate.parse(startDate);
        java.time.LocalDate end = java.time.LocalDate.parse(endDate);
        
        List<TicketTask> tasks = ticketTaskRepository.findByCompanyCodeAndDates(companyCode, start, end);
                
        String role = (String) session.getAttribute("role");
        String userId = (String) session.getAttribute("userId");
        String upperRole = (role != null) ? role.toUpperCase() : "GUEST";
        
        if (performedBy != null && !performedBy.trim().isEmpty()) {
            tasks = tasks.stream()
                    .filter(t -> performedBy.equalsIgnoreCase(t.getPerformedBy()))
                    .collect(Collectors.toList());
        } else if (!"ADMIN".equals(upperRole) && !"BUSINESS_MANAGER".equals(upperRole)) {
            tasks = tasks.stream()
                    .filter(t -> userId != null && userId.equalsIgnoreCase(t.getPerformedBy()))
                    .collect(Collectors.toList());
        }
                
        byte[] pdfData = reportExportService.exportBillableTasksToPDF(tasks, companyCode, startDate, endDate, performedBy);
        String fileName = "Billable_Tasks_" + companyCode + "_" + startDate + "_to_" + endDate + ".pdf";
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
            .header(HttpHeaders.PRAGMA, "no-cache")
            .header(HttpHeaders.EXPIRES, "0")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfData.length)
            .body(new ByteArrayResource(pdfData));
    }

    // ==================== QUICK DOWNLOAD ENDPOINTS ====================

    @GetMapping("/report-billable-tasks/quick-download/excel")
    public ResponseEntity<ByteArrayResource> quickDownloadExcel(HttpSession session) throws IOException {
        java.time.LocalDate end = java.time.LocalDate.now();
        java.time.LocalDate start = end.minusDays(30);
        return exportBillableTasksExcel(start.toString(), end.toString(), null, session);
    }

    @GetMapping("/report-billable-tasks/quick-download/pdf")
    public ResponseEntity<ByteArrayResource> quickDownloadPdf(HttpSession session) throws DocumentException {
        java.time.LocalDate end = java.time.LocalDate.now();
        java.time.LocalDate start = end.minusDays(30);
        return exportBillableTasksPdf(start.toString(), end.toString(), null, session);
    }

    @GetMapping("/report-all-tickets/excel")
    public ResponseEntity<ByteArrayResource> exportAllTicketsExcel(
            @RequestParam(required = false) String issueType,
            HttpSession session) throws IOException {
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<com.example.demo.entity.Ticket> tickets;
        String fileName;
        if ("QA".equalsIgnoreCase(issueType)) {
            // Get all tickets for this company
            tickets = ticketService.getByCompanyCode(companyCode);
            // Filter out IT_SUPPORT (QA tickets are those that are NOT IT_SUPPORT)
            tickets = tickets.stream()
                .filter(t -> t.getIssueType() == null || 
                            (!t.getIssueType().equalsIgnoreCase("IT_SUPPORT") && 
                             !t.getIssueType().equalsIgnoreCase("IT Support")))
                .collect(Collectors.toList());
            fileName = "QA_Tickets_" + companyCode + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        } else {
            tickets = ticketService.getTicketsByIssueType(companyCode, "IT_SUPPORT");
            fileName = "All_IT_Support_Tickets_" + companyCode + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        }
        
        byte[] excelData = reportExportService.exportTicketsToExcel(tickets, companyCode);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
            .header(HttpHeaders.PRAGMA, "no-cache")
            .header(HttpHeaders.EXPIRES, "0")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .contentLength(excelData.length)
            .body(new ByteArrayResource(excelData));
    }

    @GetMapping("/report-all-tickets/pdf")
    public ResponseEntity<ByteArrayResource> exportAllTicketsPdf(
            @RequestParam(required = false) String issueType,
            HttpSession session) throws DocumentException {
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<com.example.demo.entity.Ticket> tickets;
        String fileName;
        String reportTitle;
        if ("QA".equalsIgnoreCase(issueType)) {
            // Get all tickets for this company
            tickets = ticketService.getByCompanyCode(companyCode);
            // Filter out IT_SUPPORT (QA tickets are those that are NOT IT_SUPPORT)
            tickets = tickets.stream()
                .filter(t -> t.getIssueType() == null || 
                            (!t.getIssueType().equalsIgnoreCase("IT_SUPPORT") && 
                             !t.getIssueType().equalsIgnoreCase("IT Support")))
                .collect(Collectors.toList());
            fileName = "QA_Tickets_" + companyCode + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            reportTitle = "QA Tickets Report";
        } else {
            tickets = ticketService.getTicketsByIssueType(companyCode, "IT_SUPPORT");
            fileName = "All_IT_Support_Tickets_" + companyCode + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            reportTitle = "Support Tickets Report";
        }
        
        byte[] pdfData = reportExportService.exportTicketsToPDF(tickets, companyCode, reportTitle);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
            .header(HttpHeaders.PRAGMA, "no-cache")
            .header(HttpHeaders.EXPIRES, "0")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfData.length)
            .body(new ByteArrayResource(pdfData));
    }
}
