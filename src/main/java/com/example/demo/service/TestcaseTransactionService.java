package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.entity.TestCaseTransaction;
import com.example.demo.repo.TestCaseTransactionRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TestCaseTransactionService {

    private final TestCaseTransactionRepository repository;
    private final com.example.demo.repo.TestCaseHeaderRepository headerRepository;

    public TestCaseTransactionService(TestCaseTransactionRepository repository,
                                      com.example.demo.repo.TestCaseHeaderRepository headerRepository) {
        this.repository = repository;
        this.headerRepository = headerRepository;
    }

    public TestCaseTransaction save(TestCaseTransaction transaction) {
        if (transaction.getStepNo() != null) {
            if (transaction.getCombinedKey() != null && !transaction.getCombinedKey().isEmpty()) {
                transaction.setMainKey(transaction.getCombinedKey().trim() + transaction.getStepNo().trim());
            } else if (transaction.getTestcaseHeaderId() != null) {
                headerRepository.findById(transaction.getTestcaseHeaderId()).ifPresent(header -> {
                    transaction.setMainKey(header.getCombinedKey() + transaction.getStepNo().trim());
                });
            }
        }
        return repository.saveAndFlush(transaction);
    }

    public void delete(TestCaseTransaction transaction) {
        repository.delete(transaction);
    }

    public void deleteTransaction(Long id) {
        repository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<TestCaseTransaction> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<TestCaseTransaction> getByCompanyCode(String companyCode) {
        return repository.findByCompanyCode(companyCode);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<TestCaseTransaction> getTransactionsByCompanyCode(String companyCode, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findByCompanyCode(companyCode, pageable);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<TestCaseTransaction> searchTransactionsByCompanyCode(String companyCode, String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findByCompanyCode(companyCode, pageable);
        }
        return repository.searchByCompanyCodeAndKeyword(companyCode, keyword.trim(), pageable);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<TestCaseTransaction> getByTestcaseHeaderId(Long testcaseHeaderId) {
        return repository.findByTestcaseHeaderId(testcaseHeaderId);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<TestCaseTransaction> getByMainKey(String mainKey) {
        return repository.findByMainKey(mainKey);
    }

    // Generate next step number for a testcase header
    @Transactional(Transactional.TxType.SUPPORTS)
    public String generateNextStepNo(Long testcaseHeaderId) {
        String lastStepNo = repository.findLastStepNoByTestcaseHeaderId(testcaseHeaderId);
        int nextNum = Integer.parseInt(lastStepNo) + 1;
        return String.format("%02d", nextNum); // 2-digit format
    }

    public long getPassCount(String companyCode) {
        return repository.countByPassFailAndCompanyCode(companyCode, "PASS");
    }

    public long getFailCount(String companyCode) {
        return repository.countByPassFailAndCompanyCode(companyCode, "FAIL");
    }

    public long getPendingCount(String companyCode) {
        return repository.countByPendingStatusAndCompanyCode(companyCode);
    }
}