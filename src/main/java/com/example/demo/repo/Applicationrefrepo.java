package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.ApplicationRef;


public interface Applicationrefrepo extends JpaRepository<ApplicationRef, String> {
	
	// Find applications by project code
	@Query("SELECT a FROM ApplicationRef a WHERE a.project.projectCode = :projectCode")
	List<ApplicationRef> findByProjectCode(@Param("projectCode") String projectCode);
	
	// Find applications by company code (through project)
	@Query("SELECT a FROM ApplicationRef a WHERE a.project.company.companyCode = :companyCode")
	List<ApplicationRef> findByCompanyCode(@Param("companyCode") String companyCode);
	
	// Find applications by company name (case-insensitive partial match)
	@Query("SELECT a FROM ApplicationRef a WHERE a.project IS NOT NULL AND a.project.company IS NOT NULL AND LOWER(a.project.company.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))")
	List<ApplicationRef> findByCompanyNameContainingIgnoreCase(@Param("companyName") String companyName);
	
	// Find applications by company code (partial match)
	@Query("SELECT a FROM ApplicationRef a WHERE a.project IS NOT NULL AND a.project.company IS NOT NULL AND LOWER(a.project.company.companyCode) LIKE LOWER(CONCAT('%', :companyCode, '%'))")
	List<ApplicationRef> findByCompanyCodeContainingIgnoreCase(@Param("companyCode") String companyCode);
	
	// Find applications by company name or company code (case-insensitive partial match)
	@Query("SELECT a FROM ApplicationRef a WHERE a.project IS NOT NULL AND a.project.company IS NOT NULL AND (LOWER(a.project.company.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(a.project.company.companyCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
	List<ApplicationRef> findByCompanyNameOrCodeContainingIgnoreCase(@Param("searchTerm") String searchTerm);
}
