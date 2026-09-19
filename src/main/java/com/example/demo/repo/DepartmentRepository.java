package com.example.demo.repo;

import com.example.demo.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {

    List<Department> findByCompany_CompanyCode(String companyCode);

    List<Department> findByCompany_CompanyCodeIgnoreCase(String companyCode);

    List<Department> findByCompany_CompanyCodeAndStatus(String companyCode, String status);

    List<Department> findByDepartmentNameContainingIgnoreCaseOrDepartmentCodeContainingIgnoreCase(String name, String code);

    List<Department> findByCompany_CompanyCodeAndDepartmentNameContainingIgnoreCase(String companyCode, String name);
}
