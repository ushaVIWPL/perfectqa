package com.example.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.ModuleRef;

public interface ModuleRefRepository extends JpaRepository<ModuleRef, String> {

	ModuleRef findTopByOrderByModuleKeyDesc();
    Optional<ModuleRef> findByModuleKey(String moduleKey);
    
    // Find modules by application code
    @Query("SELECT m FROM ModuleRef m WHERE m.application.appCode = :appCode")
    List<ModuleRef> findByApplicationCode(@Param("appCode") String appCode);
    
    // Find modules by project (through application)
    @Query("SELECT m FROM ModuleRef m WHERE m.application.project.projectCode = :projectCode")
    List<ModuleRef> findByProjectCode(@Param("projectCode") String projectCode);
    
    // Find modules by company code (through application -> project -> company)
    @Query("SELECT m FROM ModuleRef m WHERE m.application.project.company.companyCode = :companyCode")
    List<ModuleRef> findByCompanyCode(@Param("companyCode") String companyCode);
    
    // Find modules by company name (case-insensitive partial match)
    @Query("SELECT m FROM ModuleRef m WHERE m.application IS NOT NULL AND m.application.project IS NOT NULL AND m.application.project.company IS NOT NULL AND LOWER(m.application.project.company.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))")
    List<ModuleRef> findByCompanyNameContainingIgnoreCase(@Param("companyName") String companyName);
    
    // Find modules by company code (partial match)
    @Query("SELECT m FROM ModuleRef m WHERE m.application IS NOT NULL AND m.application.project IS NOT NULL AND m.application.project.company IS NOT NULL AND LOWER(m.application.project.company.companyCode) LIKE LOWER(CONCAT('%', :companyCode, '%'))")
    List<ModuleRef> findByCompanyCodeContainingIgnoreCase(@Param("companyCode") String companyCode);
    
    // Find modules by company name or company code (case-insensitive partial match)
    @Query("SELECT m FROM ModuleRef m WHERE m.application IS NOT NULL AND m.application.project IS NOT NULL AND m.application.project.company IS NOT NULL AND (LOWER(m.application.project.company.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(m.application.project.company.companyCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<ModuleRef> findByCompanyNameOrCodeContainingIgnoreCase(@Param("searchTerm") String searchTerm);
}
