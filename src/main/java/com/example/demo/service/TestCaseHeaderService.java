package com.example.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.entity.TestCaseHeader;
import com.example.demo.repo.TestCaseHeaderRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TestCaseHeaderService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "combinedKey", "transactionKey", "testCaseNo", "activity",
            "responsible", "tCode", "testedBy", "id");

    private final TestCaseHeaderRepository repository;
    
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.demo.repo.TestCaseTransactionRepository transactionRepository;

    public TestCaseHeaderService(TestCaseHeaderRepository repository) {
        this.repository = repository;
    }

    public TestCaseHeader save(TestCaseHeader header) {
        return repository.saveAndFlush(header);
    }

    public void delete(TestCaseHeader header) {
        repository.delete(header);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public boolean hasChildren(Long id) {
        return transactionRepository.existsByTestcaseHeaderId(id);
    }

    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    // Get all (with pagination)
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<TestCaseHeader> getAll(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sortBy, sortDir));
        return repository.findAll(pageable);
    }

    // Get all (without pagination)
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<TestCaseHeader> getAll() {
        return repository.findAll();
    }

    // Get by company code (with pagination)
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<TestCaseHeader> getByCompanyCode(String companyCode, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sortBy, sortDir));
        return repository.findByCompanyCode(companyCode, pageable);
    }
    
    // Get by company code (without pagination - for backward compatibility)
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<TestCaseHeader> getByCompanyCode(String companyCode) {
        return repository.findByCompanyCode(companyCode);
    }
    
    // Search by company code and keyword (with pagination)
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<TestCaseHeader> searchByCompanyCode(String companyCode, String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findByCompanyCode(companyCode, pageable);
        }
        return repository.searchByCompanyCodeAndKeyword(companyCode, keyword.trim(), pageable);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<TestCaseHeader> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<TestCaseHeader> getByTransactionKey(String transactionKey) {
        return repository.findByTransactionKey(transactionKey);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<TestCaseHeader> findByCombinedKey(String combinedKey) {
        return repository.findByCombinedKey(combinedKey);
    }

    // Generate next test case number for a transaction key
    @Transactional(Transactional.TxType.SUPPORTS)
    public String generateNextTestCaseNo(String transactionKey) {
        String lastTestCaseNo = repository.findLastTestCaseNoByTransactionKey(transactionKey);
        int nextNum = Integer.parseInt(lastTestCaseNo) + 1;
        return String.format("%02d", nextNum); // 2-digit format
    }

    // Check for duplicate combined key within company
    @Transactional(Transactional.TxType.SUPPORTS)
    public boolean existsByCombinedKeyAndCompany(String combinedKey, String companyCode) {
        // Use custom query for explicit company code check
        return repository.existsByCombinedKeyAndCompanyCodeCustom(combinedKey, companyCode);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public boolean existsByTransactionKeyAndTestCaseNo(String transactionKey, String testCaseNo, String companyCode) {
        return repository.existsByTransactionKeyAndTestCaseNoAndCompanyCode(transactionKey, testCaseNo, companyCode);
    }

    public long getDistinctTestedByCount(String companyCode) {
        return repository.countDistinctTestedByByCompany(companyCode);
    }

    public long getDistinctTransactionKeysCount(String companyCode) {
        return repository.countDistinctTransactionKeysByCompany(companyCode);
    }

    private Sort buildSort(String sortBy, String sortDir) {
        String field = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "combinedKey";
        return sortDir.equalsIgnoreCase("desc")
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
    }
}