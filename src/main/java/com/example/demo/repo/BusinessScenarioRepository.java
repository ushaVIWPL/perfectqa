package com.example.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.BusinessScenario;
import com.example.demo.entity.BusinessScenarioId;

@Repository
public interface BusinessScenarioRepository extends JpaRepository<BusinessScenario, BusinessScenarioId> {

    // Fetch all scenarios for a specific company (with pagination) - Optimized with fetch join
    @Query("SELECT DISTINCT bs FROM BusinessScenario bs LEFT JOIN FETCH bs.company WHERE bs.id.companyCode = :companyCode")
    Page<BusinessScenario> findByIdCompanyCode(@Param("companyCode") String companyCode, Pageable pageable);
    
    // Fetch all scenarios for a specific company (without pagination - for backward compatibility)
    @Query("SELECT DISTINCT bs FROM BusinessScenario bs LEFT JOIN FETCH bs.company WHERE bs.id.companyCode = :companyCode")
    List<BusinessScenario> findByIdCompanyCode(@Param("companyCode") String companyCode);
    
    // Search scenarios by company code with keyword (for pagination) - Optimized with indexes
    @Query("SELECT DISTINCT bs FROM BusinessScenario bs LEFT JOIN FETCH bs.company WHERE bs.id.companyCode = :companyCode " +
           "AND (LOWER(bs.id.businessScenario) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(bs.scenarioDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(bs.activity) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(bs.responsible) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<BusinessScenario> searchByCompanyCodeAndKeyword(@Param("companyCode") String companyCode, 
                                                          @Param("keyword") String keyword, 
                                                          Pageable pageable);

    // Find by company code and business scenario
    Optional<BusinessScenario> findByIdCompanyCodeAndIdBusinessScenario(String companyCode, String businessScenario);

    // Get the last business scenario number for a company (for auto-generation)
    @Query("SELECT COALESCE(MAX(bs.id.businessScenario), '00') FROM BusinessScenario bs WHERE bs.id.companyCode = :companyCode")
    String findLastBusinessScenarioByCompany(@Param("companyCode") String companyCode);

    @Query("SELECT COUNT(DISTINCT bs.responsible) FROM BusinessScenario bs WHERE bs.id.companyCode = :companyCode")
    long countDistinctResponsibleByCompany(@Param("companyCode") String companyCode);

    @Query("SELECT COUNT(DISTINCT bs.workStream) FROM BusinessScenario bs WHERE bs.id.companyCode = :companyCode")
    long countDistinctWorkStreamByCompany(@Param("companyCode") String companyCode);
}
