package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.TestCaseTransaction;
import com.example.demo.repo.TestCaseTransactionRepository;


@Service
public class TestCaseTransactionService {

    @Autowired
    private TestCaseTransactionRepository repository;

    /**
     * Save (create or update) a transaction.
     * Ensures transactionId is set as mainKey + "-" + testCaseNo.
     */
    public TestCaseTransaction saveTransaction(TestCaseTransaction transaction) {
        if (transaction.getMainKey() == null || transaction.getMainKey().isBlank()) {
            throw new IllegalArgumentException("mainKey must be provided");
        }
        if (transaction.getTestCaseNo() == null || transaction.getTestCaseNo().isBlank()) {
            throw new IllegalArgumentException("testCaseNo must be provided");
        }

        String id = transaction.getMainKey().trim() + transaction.getTestCaseNo().trim();
        transaction.setTransactionId(id);

        return repository.save(transaction);
    }

    public List<TestCaseTransaction> getAllTransactions() {
        return repository.findAll();
    }

    public List<TestCaseTransaction> findByMainKey(String mainKey) {
        return repository.findByMainKey(mainKey);
    }

    public List<TestCaseTransaction> findByTestCaseNo(String testCaseNo) {
        return repository.findByTestCaseNo(testCaseNo);
    }

    public Optional<TestCaseTransaction> getTransactionById(String id) {
        return repository.findById(id);
    }

    public void deleteTransaction(String id) {
        repository.deleteById(id);
    }
    
    public TestCaseTransaction save(TestCaseTransaction transaction) {
        return repository.save(transaction);
    }
    
    
    public TestCaseTransaction updateTransactionFiles(TestCaseTransaction existing, List<MultipartFile> files) throws IOException {
        Path screenshotsDir = Paths.get("uploads", "screenshots");
        Files.createDirectories(screenshotsDir);

        List<String> savedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String safeName = UUID.randomUUID() + "_" +
                        Objects.requireNonNull(file.getOriginalFilename()).replaceAll("[^a-zA-Z0-9._-]", "_");
                Files.copy(file.getInputStream(), screenshotsDir.resolve(safeName), StandardCopyOption.REPLACE_EXISTING);
                savedFiles.add(safeName);
            }
        }

        if (!savedFiles.isEmpty()) {
            if (existing.getScreenshotPaths() != null && !existing.getScreenshotPaths().isBlank()) {
                existing.setScreenshotPaths(existing.getScreenshotPaths() + "," + String.join(",", savedFiles));
            } else {
                existing.setScreenshotPaths(String.join(",", savedFiles));
            }
        }

        return repository.save(existing);
    }
    
    
    public TestCaseTransaction updateTransaction(String transactionId,
            TestCaseTransaction updatedTx,
            List<MultipartFile> files) throws Exception {
TestCaseTransaction existing = repository.findById(transactionId)
.orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

// Update text fields
existing.setTestCaseNo(updatedTx.getTestCaseNo());
existing.setType(updatedTx.getType());
existing.setAction(updatedTx.getAction());
existing.setUrl(updatedTx.getUrl());
existing.setComments(updatedTx.getComments());

// Handle screenshots if files uploaded
if (files != null && !files.isEmpty()) {
List<String> base64Screenshots = new ArrayList<>();
for (MultipartFile file : files) {
String base64 = "data:" + file.getContentType() + ";base64," +
Base64.getEncoder().encodeToString(file.getBytes());
base64Screenshots.add(base64);
}
existing.setBase64Screenshots(base64Screenshots);
}

return repository.save(existing);
}

 
    
}
