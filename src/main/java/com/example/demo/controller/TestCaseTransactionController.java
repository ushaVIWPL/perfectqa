package com.example.demo.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.TestCaseTransaction;
import com.example.demo.repo.TestCaseTransactionRepository;
import com.example.demo.service.TestCaseTransactionService;

import jakarta.servlet.http.HttpSession;

@Controller
public class TestCaseTransactionController {

    @Autowired
    private TestCaseTransactionService service;
    
    @Autowired
    private TestCaseTransactionRepository  repo;


    // configurable upload root; default "uploads"
    @Value("${app.upload-dir:uploads}")
    private String uploadRoot;

    /**
     * Show Add form from header button. URL: /showAddTransactionForm?combinedKey=HEADER123
     * This pre-fills mainKey with header combinedKey.
     */
    @GetMapping("/showAddTransactionForm")
    public String showAddTransactionForm(@RequestParam("combinedKey") String combinedKey, Model model) {
        TestCaseTransaction tx = new TestCaseTransaction();
        tx.setMainKey(combinedKey);
        model.addAttribute("transaction", tx);
        return "addtransactionform"; // your existing Thymeleaf template name
    }

    /**
     * Save endpoint (handles both create and update). Accepts POST to /save or /saveTransaction
     */
    @PostMapping({"/save", "/saveTransaction"})
    public String saveTransaction(@ModelAttribute TestCaseTransaction transaction,
                                  @RequestParam(value = "uploadedScreenshots", required = false) List<MultipartFile> files,
                                  RedirectAttributes redirectAttrs) {

        try {
            // === store files (if any) ===
            List<String> savedFileNames = new ArrayList<>();
            if (files != null && !files.isEmpty()) {
                Path screenshotsDir = Paths.get(uploadRoot, "screenshots");
                Files.createDirectories(screenshotsDir);

                for (MultipartFile file : files) {
                    if (file != null && !file.isEmpty()) {
                        String original = Objects.requireNonNull(file.getOriginalFilename());
                        String safe = original.replaceAll("[^a-zA-Z0-9._-]", "_");
                        String filename = UUID.randomUUID().toString() + "_" + safe;
                        Path target = screenshotsDir.resolve(filename);
                        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                        savedFileNames.add(filename);
                    }
                }
            }

            // === merge with any existing screenshots (when editing) ===
            if (!savedFileNames.isEmpty()) {
                String existing = transaction.getScreenshotPaths();
                if (existing != null && !existing.isBlank()) {
                    List<String> merged = new ArrayList<>(Arrays.asList(existing.split(",")));
                    merged.addAll(savedFileNames);
                    transaction.setScreenshotPaths(String.join(",", merged));
                } else {
                    transaction.setScreenshotPaths(String.join(",", savedFileNames));
                }
            }

            // === save transaction (service builds transactionId = mainKey-testCaseNo) ===
            service.saveTransaction(transaction);

            redirectAttrs.addFlashAttribute("successMessage", "Transaction saved successfully");
            // redirect back to list for same mainKey
            return "redirect:/getall?mainKey=" + transaction.getMainKey();

        } catch (IOException ex) {
            redirectAttrs.addFlashAttribute("error", "Failed to save files: " + ex.getMessage());
            return "redirect:/showAddTransactionForm?combinedKey=" + transaction.getMainKey();
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("error", "Error: " + ex.getMessage());
            return "redirect:/showAddTransactionForm?combinedKey=" + transaction.getMainKey();
        }
    }

    /**
     * List transactions. If mainKey provided, filter by mainKey, else all.
     * URL: /getall or /getall?mainKey=HEADER123
     */
	/*
	 * @GetMapping("/getall") public String listTransactions(@RequestParam(name =
	 * "mainKey", required = false) String mainKey, Model model) {
	 * List<TestCaseTransaction> list; if (mainKey != null && !mainKey.isBlank()) {
	 * list = service.findByMainKey(mainKey); model.addAttribute("mainKey",
	 * mainKey); } else { list = service.getAllTransactions(); }
	 * model.addAttribute("transactions", list); return "listtransactions"; }
	 */
     /**
     * Edit (open form with existing data). URL: /editTransaction?id=MAIN-01
     */
    private String encodeFileToBase64(Path filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(filePath);
        return Base64.getEncoder().encodeToString(bytes);
    }

    
    @GetMapping("/getall")
    public String listTransactions(@RequestParam(name = "mainKey", required = false) String mainKey,
                                   Model model,
                                   Principal principal,
                                   HttpSession session) {

        List<TestCaseTransaction> list;
        if (mainKey != null && !mainKey.isBlank()) {
            list = service.findByMainKey(mainKey);
            model.addAttribute("mainKey", mainKey);
        } else {
            list = service.getAllTransactions();
        }

        // Convert screenshot paths into base64 strings
        for (TestCaseTransaction tx : list) {
            if (tx.getScreenshotPaths() != null && !tx.getScreenshotPaths().isBlank()) {
                List<String> base64List = new ArrayList<>();
                for (String file : tx.getScreenshotPaths().split(",")) {
                    try {
                        Path filePath = Paths.get(uploadRoot, "screenshots", file.trim());
                        String base64 = encodeFileToBase64(filePath);
                        base64List.add("data:image/png;base64," + base64);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                tx.setBase64Screenshots(base64List);
            }
        }

        // Set role inline
        String role = "tester"; // default
        if (principal != null) {
            String username = principal.getName();
            // Example: inline role mapping
            if (username.equalsIgnoreCase("admin")) {
                role = "admin";
            } else if (username.equalsIgnoreCase("qa")) {
                role = "qa";
            }
            // All other users default to "tester"
        }

        model.addAttribute("role", role);
        model.addAttribute("transactions", list);

        return "listtransactions";
    }

	/*
	 * @GetMapping("/editTransaction") public String
	 * editTransaction(@RequestParam("id") String transactionId, Model model,
	 * RedirectAttributes redirectAttrs) { Optional<TestCaseTransaction> opt =
	 * service.getTransactionById(transactionId); if (opt.isEmpty()) {
	 * redirectAttrs.addFlashAttribute("error", "Transaction not found: " +
	 * transactionId); return "redirect:/getall"; }
	 * model.addAttribute("transaction", opt.get()); return "addtransactionform"; //
	 * reuse the add form for editing }
	 */
    @GetMapping("/editTransaction/{id}")
    public String editTransaction(@PathVariable("id") String transactionId, Model model, RedirectAttributes redirectAttrs) {
        Optional<TestCaseTransaction> opt = service.getTransactionById(transactionId);
        if (opt.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Transaction not found: " + transactionId);
            return "redirect:/getall";
        }

        TestCaseTransaction tx = opt.get();
        model.addAttribute("transaction", tx);

        List<Map<String, String>> screenshots = new ArrayList<>();

        if (tx.getScreenshotPaths() != null && !tx.getScreenshotPaths().isEmpty()) {
            for (String fileName : tx.getScreenshotPaths().split(",")) {
                Path filePath = Paths.get("uploads/screenshots", fileName.trim());
                if (Files.exists(filePath)) {
                    try {
                        byte[] bytes = Files.readAllBytes(filePath);
                        String base64 = Base64.getEncoder().encodeToString(bytes);
                        
                        Map<String, String> fileMap = new HashMap<>();
                        fileMap.put("name", fileName.trim());
                        fileMap.put("base64", "data:image/png;base64," + base64);
                        
                        screenshots.add(fileMap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        model.addAttribute("screenshots", screenshots);
        return "editTransaction";
    }


    
    
    @GetMapping("/deleteScreenshot/{id}/{fileName}")
    public String deleteScreenshot(@PathVariable String id, @PathVariable String fileName, RedirectAttributes redirectAttrs) {
        Optional<TestCaseTransaction> opt = service.getTransactionById(id);
        if (opt.isPresent()) {
            TestCaseTransaction tx = opt.get();
            // Remove file from disk
            Path filePath = Paths.get("uploads/screenshots", fileName);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Remove file name from transaction
            List<String> files = new ArrayList<>(Arrays.asList(tx.getScreenshotPaths().split(",")));
            files.remove(fileName);
            tx.setScreenshotPaths(String.join(",", files));
            service.saveTransaction(tx); // save updated transaction
        }
        redirectAttrs.addFlashAttribute("successMessage", "Screenshot deleted successfully");
        return "redirect:/editTransaction/" + id;
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    /**
     * Delete a transaction. URL: /deleteTransaction?id=MAIN-01
     */
    @GetMapping("/deleteTransaction")
    public String deleteTransaction(@RequestParam("id") String transactionId, RedirectAttributes redirectAttrs) {
        service.deleteTransaction(transactionId);
        redirectAttrs.addFlashAttribute("successMessage", "Deleted: " + transactionId);
        return "redirect:/getall";
    }

    /**
     * A small JSON API to fetch all transactions (optional)
     */
    @GetMapping("/api/transactions")
    @ResponseBody
    public List<TestCaseTransaction> apiAll() {
        return service.getAllTransactions();
    }
    
    @PutMapping("/api/transactions/update/{id}")
    @ResponseBody
    public ResponseEntity<TestCaseTransaction> updateTransaction(
            @PathVariable String id,
            @RequestBody TestCaseTransaction updatedTx) {
        
        return service.getTransactionById(id)
                .map(existing -> {
                    existing.setType(updatedTx.getType());
                    existing.setAction(updatedTx.getAction());
                    existing.setUrl(updatedTx.getUrl());          // ✅ save URL
                    existing.setComments(updatedTx.getComments()); // ✅ save comments
                    // ⚡ keep screenshotPaths as-is (no inline editing for files)

                    TestCaseTransaction saved = service.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    
    
    
	/*
	 * put mapping for image
	 */    
    
//    @PutMapping("/api/transactions/{transactionId}/screenshots")
//    public ResponseEntity<Map<String, Object>> updateScreenshots(
//            @PathVariable String transactionId,
//            @RequestParam("files") List<MultipartFile> files) {
//
//        try {
//            TestCaseTransaction updated = service.updateTransaction(transactionId, new TestCaseTransaction(), files);
//
//            return ResponseEntity.ok(Map.of(
//                    "message", "Screenshots updated successfully!",
//                    "transaction", updated
//            ));
//
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(404)
//                    .body(Map.of("message", "Transaction not found for ID: " + transactionId));
//        } catch (Exception e) {
//            return ResponseEntity.status(500)
//                    .body(Map.of("message", "Internal server error while updating screenshots"));
//        }
//    }
    
    
    
    
    @PostMapping("/editTransactionform")
    public String saveTransactionEdit(
            @ModelAttribute TestCaseTransaction transaction,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "existingFileNames", required = false) String existingFileNames,
            Model model) {

        System.out.println("✅ Updating TestCaseTransaction...");
        System.out.println("➡ Main Key: " + transaction.getMainKey());
        System.out.println("➡ Transaction No: " + transaction.getTestCaseNo());
        System.out.println("➡ Existing Screenshots: " + existingFileNames);
        System.out.println("➡ Uploaded New Files Count: " + (files != null ? files.size() : 0));

        // Upload directory
        String uploadDir = "uploads/screenshots/";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        List<String> allFiles = new ArrayList<>();

        // Preserve existing file names
        if (existingFileNames != null && !existingFileNames.isBlank()) {
            List<String> existingList = List.of(existingFileNames.split(","));
            allFiles.addAll(existingList);
            System.out.println("🧾 Preserving existing files: " + existingList);
        }

        // Handle new file uploads
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                        Path path = Paths.get(uploadDir, uniqueName);
                        Files.write(path, file.getBytes());
                        allFiles.add(uniqueName);
                        System.out.println("📸 Uploaded new file: " + uniqueName);
                    } catch (IOException e) {
                        e.printStackTrace();
                        model.addAttribute("error", "Error saving file: " + file.getOriginalFilename());
                        return "editTransaction";
                    }
                }
            }
        }

        // Update the entity with all screenshot paths
        transaction.setScreenshotPaths(String.join(",", allFiles));

        // Save transaction
        service.saveTransaction(transaction);
        System.out.println("✅ Saved TestCaseTransaction with main key: " + transaction.getMainKey());

        model.addAttribute("transaction", transaction);
        model.addAttribute("successMessage", "Test case transaction updated successfully!");

        // Rebuild screenshots list with Base64
        List<Map<String, String>> screenshots = new ArrayList<>();
        for (String fileName : allFiles) {
            Path path = Paths.get(uploadDir, fileName.trim());
            if (Files.exists(path)) {
                try {
                    byte[] bytes = Files.readAllBytes(path);
                    String base64 = Base64.getEncoder().encodeToString(bytes);

                    Map<String, String> fileMap = new HashMap<>();
                    fileMap.put("name", fileName.trim());
                    fileMap.put("base64", "data:image/png;base64," + base64);

                    screenshots.add(fileMap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        model.addAttribute("screenshots", screenshots);

        return "editTransaction";
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    @GetMapping("/api/transactions/{id}")
    @ResponseBody
    public ResponseEntity<?> getTransaction(@PathVariable String id) {
        Optional<TestCaseTransaction> opt = service.getTransactionById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found");
        }
        return ResponseEntity.ok(opt.get()); // or DTO with base64Screenshots
    }

    
    
    
    
}
