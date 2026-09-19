package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "department_ref")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    @Id
    @Column(name = "department_code", nullable = false, unique = true)
    private String departmentCode;

    @Column(name = "department_name", nullable = false)
    private String departmentName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "head_of_department")
    private String headOfDepartment;

    @Column(name = "status")
    private String status; // e.g., "Active" or "Inactive"

    @ManyToOne
    @JoinColumn(name = "company_code")
    private CompanyRef company;

    // Explicit getters and setters for compatibility
    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHeadOfDepartment() {
        return headOfDepartment;
    }

    public void setHeadOfDepartment(String headOfDepartment) {
        this.headOfDepartment = headOfDepartment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public CompanyRef getCompany() {
        return company;
    }

    public void setCompany(CompanyRef company) {
        this.company = company;
    }
}
