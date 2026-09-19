package com.example.demo.controller;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.TestCaseHeader;
import com.example.demo.entity.TestCaseTransaction;
import com.example.demo.repo.TestCaseTransactionRepository;
import com.example.demo.service.TestCaseHeaderService;
import com.example.demo.service.TestCaseTransactionService;
import com.example.demo.util.RoleAccessUtil;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/api")
public class TestCaseTransactionController {

    @Autowired
    private TestCaseTransactionRepository repo;

    private final TestCaseHeaderService headerService;
    private final TestCaseTransactionService transactionService;

    public TestCaseTransactionController(
            TestCaseHeaderService headerService,
            TestCaseTransactionService transactionService) {
        this.headerService = headerService;
        this.transactionService = transactionService;
    }

    @GetMapping("/add-transaction")
    public String openAddTransactionForm(
            @RequestParam String combinedKey,
            @RequestParam String companyCode,
            Model model,
            HttpSession session) {
        System.out.println("Combined Key: " + combinedKey);

        Optional<TestCaseHeader> headerOpt = headerService.findByCombinedKey(combinedKey);

        if (headerOpt.isEmpty()) {
            model.addAttribute("error", "Test case header not found for combined key: " + combinedKey);
            return "redirect:/api/transactions/testcaseheaders";
        }

        session.setAttribute("companyCode", companyCode);

        TestCaseTransaction transaction = new TestCaseTransaction();
        transaction.setCompanyCode(companyCode);
        transaction.setTestcaseHeaderId(headerOpt.get().getId());
        transaction.setMainKey(combinedKey);

        model.addAttribute("transaction", transaction);
        return "addtransactionform";
    }


    @PostMapping("/save-transaction")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveTransaction(
            @ModelAttribute("transaction") TestCaseTransaction transaction,
            @RequestParam(value = "screenshotFiles", required = false) MultipartFile[] files,
            HttpSession session) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String companyCode = (String) session.getAttribute("companyCode");
            if (companyCode == null) {
                response.put("success", false);
                response.put("message", "Session expired or company code missing. Please log in again.");
                return ResponseEntity.ok(response);
            }

            transaction.setCompanyCode(companyCode);
            transaction.setCombinedKey(transaction.getMainKey());

            // Check duplicate step number (add mode)
            if (transaction.getCombinedKey() != null && transaction.getStepNo() != null) {
                String fullMainKey = transaction.getCombinedKey().trim() + transaction.getStepNo().trim();
                List<TestCaseTransaction> existing = transactionService.getByMainKey(fullMainKey);
                boolean exists = existing.stream().anyMatch(t -> companyCode.equals(t.getCompanyCode()));
                if (exists) {
                    response.put("success", false);
                    response.put("message", "Transaction with this step number already exists for this test case.");
                    return ResponseEntity.ok(response);
                }
            }

            if (files != null && files.length > 0) {
                StringBuilder savedPaths = new StringBuilder();
                String uploadDir = Paths.get("uploads", "screenshots").toAbsolutePath().toString();
                new File(uploadDir).mkdirs();
                
                for (MultipartFile file : files) {
                    if (file != null && !file.isEmpty()) {
                        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                        File destination = new File(uploadDir, fileName);
                        file.transferTo(destination);
                        
                        if (savedPaths.length() > 0) {
                            savedPaths.append(",");
                        }
                        savedPaths.append("/uploads/screenshots/").append(fileName);
                    }
                }
                if (savedPaths.length() > 0) {
                    transaction.setScreenshotPaths(savedPaths.toString());
                }
            }

            transactionService.save(transaction);
            response.put("success", true);
            response.put("message", "Transaction saved successfully!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error saving transaction: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

	/*
	 * @GetMapping("/all-transactions") public String listAllTransactions(Model
	 * model, HttpSession session) {
	 * 
	 * String companyCode = (String) session.getAttribute("companyCode");
	 * 
	 * List<TestCaseTransaction> transactions; if (companyCode != null &&
	 * !companyCode.isBlank()) { transactions =
	 * transactionService.getByCompanyCode(companyCode); } else { transactions =
	 * transactionService.getAllTransactions(); }
	 * 
	 * model.addAttribute("transactions", transactions);
	 * model.addAttribute("companyCode", companyCode);
	 * 
	 * return "listtransactions"; }
	 */

    @GetMapping("/transactions")
    public String listTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            HttpSession session, 
            Model model) {

        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "Company Code not found in session.");
            return "transaction_list";
        }

        org.springframework.data.domain.Page<TestCaseTransaction> transactionsPage;
        if (search != null && !search.trim().isEmpty()) {
            transactionsPage = transactionService.searchTransactionsByCompanyCode(companyCode, search, page, size, sortBy, sortDir);
        } else {
            transactionsPage = transactionService.getTransactionsByCompanyCode(companyCode, page, size, sortBy, sortDir);
        }

        model.addAttribute("transactions", transactionsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactionsPage.getTotalPages());
        model.addAttribute("totalItems", transactionsPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("companyCode", companyCode);
        
        // Add metrics for dashboard boxes
        model.addAttribute("passCount", transactionService.getPassCount(companyCode));
        model.addAttribute("failCount", transactionService.getFailCount(companyCode));
        model.addAttribute("pendingCount", transactionService.getPendingCount(companyCode));
        
        return "listtransactions";
    }

    @PostMapping("/update-transaction")
    public String updateTransaction(
            @ModelAttribute("transaction") TestCaseTransaction transaction,
            @RequestParam(value = "screenshotFiles", required = false) MultipartFile[] files) {

        try {
            if (files != null && files.length > 0) {
                String uploadDir = "uploads/screenshots/";
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                StringBuilder savedPaths = new StringBuilder();
                // Preserve existing screenshot paths if present
                Optional<TestCaseTransaction> existingOpt = repo.findById(transaction.getId());
                if (existingOpt.isPresent() && existingOpt.get().getScreenshotPaths() != null) {
                    savedPaths.append(existingOpt.get().getScreenshotPaths());
                }

                for (MultipartFile file : files) {
                    if (file != null && !file.isEmpty()) {
                        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                        String filePath = uploadDir + fileName;
                        file.transferTo(new File(filePath));
                        
                        if (savedPaths.length() > 0) {
                            savedPaths.append(",");
                        }
                        savedPaths.append("/uploads/screenshots/").append(fileName);
                    }
                }
                
                if (savedPaths.length() > 0) {
                    transaction.setScreenshotPaths(savedPaths.toString());
                }
            } else {
                // Preserve existing screenshot paths if no files uploaded
                Optional<TestCaseTransaction> existingOpt = repo.findById(transaction.getId());
                existingOpt.ifPresent(existing -> transaction.setScreenshotPaths(existing.getScreenshotPaths()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        repo.save(transaction);
        return "redirect:/api/all-transactions";
    }

    @GetMapping("/testcase-transactions/exists")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkTransactionExists(
            @RequestParam String mainKey,
            @RequestParam String stepNo,
            HttpSession session) {
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty() || 
            mainKey == null || mainKey.isEmpty() || 
            stepNo == null || stepNo.isEmpty()) {
            return ResponseEntity.ok(Map.of("exists", false));
        }
        
        String fullMainKey = mainKey.trim() + stepNo.trim();
        List<TestCaseTransaction> existing = transactionService.getByMainKey(fullMainKey);
        
        boolean exists = existing.stream().anyMatch(t -> companyCode.equals(t.getCompanyCode()));
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping("/ajax/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteTransaction(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String role = RoleAccessUtil.getCurrentRole(session);
            if (!"ADMIN".equals(role) && !"BUSINESS_MANAGER".equals(role)) {
                response.put("success", false);
                response.put("message", "You don't have permission to delete.");
                return ResponseEntity.ok(response);
            }
            
            String companyCode = (String) session.getAttribute("companyCode");
            if (companyCode == null || companyCode.isEmpty()) {
                response.put("success", false);
                response.put("message", "Session expired or company code missing.");
                return ResponseEntity.ok(response);
            }

            Optional<TestCaseTransaction> transactionOpt = transactionService.findById(id);
            if (transactionOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Transaction not found.");
                return ResponseEntity.ok(response);
            }

            transactionService.deleteTransaction(id);
            response.put("success", true);
            response.put("message", "Transaction deleted successfully.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error deleting transaction: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
