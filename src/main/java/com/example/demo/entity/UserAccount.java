package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @Column(name = "userid", length = 255)
    private String userId;

    @Column(name = "password", length = 100)
    private String password;

    private String firstName;
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    private String phoneNo;
    private String whatsappNo;
    private String requesterName;

    private LocalDateTime createdDate = LocalDateTime.now();
    private Integer status = 1;  // Default: 1 = Active, 0 = Inactive
    private LocalDateTime deactivatedDate;

    private String accessLevel1;
    private String accessLevel2;
    private String accessLevel3;

    private String countryCode;
    private String countryName;

    private String division;
    private String department;
    private String location;

    private String userTitle;
    private String userRole;
    private String reportingManager;

    private String language;
    private String emergencyContactNo;
    private String license;
    private String resetToken;
 // getter and setter
 public String getWhatsappNo() { return whatsappNo; }
 public void setWhatsappNo(String whatsappNo) { this.whatsappNo = whatsappNo; }
    // --- Foreign Key to CompanyRef ---
    @ManyToOne
    @JoinColumn(name = "company_code") // foreign key column in user_accounts table
    private CompanyRef company;

    // Convenience methods to access companyCode and companyName
    public String getCompanyCode() {
        return company != null ? company.getCompanyCode() : null;
    }

    public String getCompanyName() {
        return company != null ? company.getCompanyName() : null;
    }

}
