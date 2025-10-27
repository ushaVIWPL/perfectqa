package com.example.demo.entity;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @Column(name = "UserID", length = 15, unique = true)
    private String userId;

    @Column(name = "Password", length = 50)
    private String password;

    private String firstName;
    private String lastName;
    
    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNo;
    private String requesterName;

    private LocalDateTime createdDate = LocalDateTime.now();
    private Integer status;
    private LocalDateTime deactivatedDate;

    private String accessLevel1;
    private String accessLevel2;
    private String accessLevel3;

    private String countryCode;
    private String countryName;

    private String companyCode;
    private String companyName;

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

    // Getters & setters
}
