package com.example.demo.repo;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.TestCaseTransaction;

public interface TestCaseTransactionRepository extends JpaRepository<TestCaseTransaction, Long> {

    // Get all transactions for a company
    List<TestCaseTransaction> findByCompanyCode(String companyCode);
    
    // Get all transactions for a company (with pagination)
    Page<TestCaseTransaction> findByCompanyCode(String companyCode, Pageable pageable);

    // Search transactions by company code and keyword
    @Query("SELECT t FROM TestCaseTransaction t WHERE t.companyCode = :companyCode AND " +
           "(LOWER(t.mainKey) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.stepNo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.action) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.comments) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<TestCaseTransaction> searchByCompanyCodeAndKeyword(@Param("companyCode") String companyCode, @Param("keyword") String keyword, Pageable pageable);

    // Get transactions by testcase header ID
    List<TestCaseTransaction> findByTestcaseHeaderId(Long testcaseHeaderId);

    // Get transactions by main key
    List<TestCaseTransaction> findByMainKey(String mainKey);

    // Get the last step number for a testcase header (for auto-generation)
    @Query("SELECT COALESCE(MAX(t.stepNo), '00') FROM TestCaseTransaction t WHERE t.testcaseHeaderId = :testcaseHeaderId")
    String findLastStepNoByTestcaseHeaderId(@Param("testcaseHeaderId") Long testcaseHeaderId);

    // Check if transaction exists
    boolean existsById(Long id);

    @Query("SELECT COUNT(t) FROM TestCaseTransaction t WHERE t.companyCode = :companyCode AND t.passFail = :status")
    long countByPassFailAndCompanyCode(@Param("companyCode") String companyCode, @Param("status") String status);

    @Query("SELECT COUNT(t) FROM TestCaseTransaction t WHERE t.companyCode = :companyCode AND (t.passFail IS NULL OR t.passFail = '')")
    long countByPendingStatusAndCompanyCode(@Param("companyCode") String companyCode);

    boolean existsByTestcaseHeaderId(Long testcaseHeaderId);
}