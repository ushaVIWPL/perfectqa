package com.example.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.TestCaseHeader;

public interface TestCaseHeaderRepository extends JpaRepository<TestCaseHeader, Long> {

    // Get all test case headers for a company (with pagination) - Optimized with fetch join
    @Query("SELECT tch FROM TestCaseHeader tch WHERE tch.companyCode = :companyCode")
    Page<TestCaseHeader> findByCompanyCode(@Param("companyCode") String companyCode, Pageable pageable);
    
    // Get all test case headers for a company (without pagination - for backward compatibility)
    @Query("SELECT DISTINCT tch FROM TestCaseHeader tch LEFT JOIN FETCH tch.scenarioActivity WHERE tch.companyCode = :companyCode")
    List<TestCaseHeader> findByCompanyCode(@Param("companyCode") String companyCode);
    
    // Search test case headers by company code with keyword (for pagination) - Optimized with indexes
    @Query("SELECT tch FROM TestCaseHeader tch WHERE tch.companyCode = :companyCode " +
           "AND (LOWER(tch.transactionKey) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(tch.testCaseNo) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(tch.combinedKey) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(tch.activity) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(tch.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<TestCaseHeader> searchByCompanyCodeAndKeyword(@Param("companyCode") String companyCode, 
                                                        @Param("keyword") String keyword, 
                                                        Pageable pageable);

    // Get test case headers by transaction key
    List<TestCaseHeader> findByTransactionKey(String transactionKey);

    // Find by combined key
    Optional<TestCaseHeader> findByCombinedKey(String combinedKey);

    // Get the last test case number for a transaction key (for auto-generation)
    @Query("SELECT COALESCE(MAX(t.testCaseNo), '00') FROM TestCaseHeader t WHERE t.transactionKey = :transactionKey")
    String findLastTestCaseNoByTransactionKey(@Param("transactionKey") String transactionKey);

    // Check if test case header exists by combined key and company
    boolean existsByCombinedKeyAndCompanyCode(String combinedKey, String companyCode);
    
    // Custom query to explicitly check for duplicate combined key within same company
    @Query("SELECT COUNT(t) > 0 FROM TestCaseHeader t WHERE t.combinedKey = :combinedKey AND t.companyCode = :companyCode")
    boolean existsByCombinedKeyAndCompanyCodeCustom(@Param("combinedKey") String combinedKey, @Param("companyCode") String companyCode);

    // Robust check for duplicate using raw fields rather than generated combined key
    boolean existsByTransactionKeyAndTestCaseNoAndCompanyCode(String transactionKey, String testCaseNo, String companyCode);

    @Query("SELECT COUNT(DISTINCT tch.testedBy) FROM TestCaseHeader tch WHERE tch.companyCode = :companyCode AND tch.testedBy IS NOT NULL AND tch.testedBy != ''")
    long countDistinctTestedByByCompany(@Param("companyCode") String companyCode);

    @Query("SELECT COUNT(DISTINCT tch.transactionKey) FROM TestCaseHeader tch WHERE tch.companyCode = :companyCode")
    long countDistinctTransactionKeysByCompany(@Param("companyCode") String companyCode);

    boolean existsByTransactionKey(String transactionKey);
}