package com.example.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.embeddedid.ScenarioActivitiesId;
import com.example.demo.entity.ScenarioActivities;

@Repository
public interface ScenarioActivitiesRepository extends JpaRepository<ScenarioActivities, ScenarioActivitiesId> {

    // Get all activities for a given business scenario and company (with pagination) - Optimized with fetch join
    @Query("SELECT DISTINCT sa FROM ScenarioActivities sa LEFT JOIN FETCH sa.businessScenario " +
           "WHERE sa.id.businessScenarioId.companyCode = :companyCode " +
           "AND sa.id.businessScenarioId.businessScenario = :businessScenario")
    Page<ScenarioActivities> findByIdBusinessScenarioIdCompanyCodeAndIdBusinessScenarioIdBusinessScenario(
            @Param("companyCode") String companyCode, 
            @Param("businessScenario") String businessScenario, 
            Pageable pageable);
    
    // Get all activities for a given business scenario and company (without pagination - for backward compatibility)
    @Query("SELECT DISTINCT sa FROM ScenarioActivities sa LEFT JOIN FETCH sa.businessScenario " +
           "WHERE sa.id.businessScenarioId.companyCode = :companyCode " +
           "AND sa.id.businessScenarioId.businessScenario = :businessScenario")
    List<ScenarioActivities> findByIdBusinessScenarioIdCompanyCodeAndIdBusinessScenarioIdBusinessScenario(
            @Param("companyCode") String companyCode, 
            @Param("businessScenario") String businessScenario);

    // Get all activities for a company (with pagination) - Optimized with fetch join
    @Query("SELECT DISTINCT sa FROM ScenarioActivities sa LEFT JOIN FETCH sa.businessScenario " +
           "WHERE sa.id.businessScenarioId.companyCode = :companyCode")
    Page<ScenarioActivities> findByIdBusinessScenarioIdCompanyCode(@Param("companyCode") String companyCode, Pageable pageable);
    
    // Get all activities for a company (without pagination - for backward compatibility)
    @Query("SELECT DISTINCT sa FROM ScenarioActivities sa LEFT JOIN FETCH sa.businessScenario " +
           "WHERE sa.id.businessScenarioId.companyCode = :companyCode")
    List<ScenarioActivities> findByIdBusinessScenarioIdCompanyCode(@Param("companyCode") String companyCode);
    
    // Search activities by company code with keyword (for pagination) - Optimized with indexes
    @Query("SELECT DISTINCT sa FROM ScenarioActivities sa LEFT JOIN FETCH sa.businessScenario " +
           "WHERE sa.id.businessScenarioId.companyCode = :companyCode " +
           "AND (LOWER(sa.id.businessScenarioId.businessScenario) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(sa.id.transactionSuffix) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(sa.activity) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(sa.responsible) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(sa.transactionKey) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ScenarioActivities> searchByCompanyCodeAndKeyword(@Param("companyCode") String companyCode, 
                                                           @Param("keyword") String keyword, 
                                                           Pageable pageable);

    // Find by transaction key - Optimized with fetch join
    @Query("SELECT DISTINCT sa FROM ScenarioActivities sa LEFT JOIN FETCH sa.businessScenario " +
           "WHERE sa.transactionKey = :transactionKey")
    Optional<ScenarioActivities> findByTransactionKey(@Param("transactionKey") String transactionKey);

    // Get the last transaction suffix for a business scenario (for auto-generation)
    @Query("SELECT COALESCE(MAX(sa.id.transactionSuffix), '00') FROM ScenarioActivities sa " +
           "WHERE sa.id.businessScenarioId.companyCode = :companyCode " +
           "AND sa.id.businessScenarioId.businessScenario = :businessScenario")
    String findLastTransactionSuffix(@Param("companyCode") String companyCode,
                                   @Param("businessScenario") String businessScenario);

    @Query("SELECT COUNT(DISTINCT sa.id.businessScenarioId.businessScenario) FROM ScenarioActivities sa " +
           "WHERE sa.id.businessScenarioId.companyCode = :companyCode")
    long countDistinctBusinessScenariosByCompany(@Param("companyCode") String companyCode);

    @Query("SELECT COUNT(DISTINCT sa.transactionKey) FROM ScenarioActivities sa " +
           "WHERE sa.id.businessScenarioId.companyCode = :companyCode")
    long countDistinctTransactionKeysByCompany(@Param("companyCode") String companyCode);

	List<ScenarioActivities> findById_BusinessScenarioId_CompanyCode(String companyCode);

    boolean existsByIdBusinessScenarioIdCompanyCodeAndIdBusinessScenarioIdBusinessScenario(String companyCode, String businessScenario);
}
