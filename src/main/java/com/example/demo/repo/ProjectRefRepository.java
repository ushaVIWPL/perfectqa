package com.example.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.ProjectRef;

public interface ProjectRefRepository extends JpaRepository<ProjectRef, String> {

	Optional<ProjectRef> findByProjectCode(String projectCode);
	
	// Find projects by company code
	@Query("SELECT p FROM ProjectRef p WHERE p.company.companyCode = :companyCode")
	List<ProjectRef> findByCompanyCode(@Param("companyCode") String companyCode);
	
	// Find projects by company name (case-insensitive partial match)
	@Query("SELECT p FROM ProjectRef p WHERE p.company IS NOT NULL AND LOWER(p.company.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))")
	List<ProjectRef> findByCompanyNameContainingIgnoreCase(@Param("companyName") String companyName);
	
	// Find projects by company code (partial match)
	@Query("SELECT p FROM ProjectRef p WHERE p.company IS NOT NULL AND LOWER(p.company.companyCode) LIKE LOWER(CONCAT('%', :companyCode, '%'))")
	List<ProjectRef> findByCompanyCodeContainingIgnoreCase(@Param("companyCode") String companyCode);
	
	// Find projects by company name or company code (case-insensitive partial match)
	@Query("SELECT p FROM ProjectRef p WHERE p.company IS NOT NULL AND (LOWER(p.company.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.company.companyCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
	List<ProjectRef> findByCompanyNameOrCodeContainingIgnoreCase(@Param("searchTerm") String searchTerm);
}
