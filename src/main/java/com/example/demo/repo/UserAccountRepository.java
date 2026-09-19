package com.example.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    UserAccount findByUserId(String userId);
    @Query(value = "SELECT MAX(UserID) FROM UserAccounts", nativeQuery = true)
    String findLastUserId();

    Optional<UserAccount> findByEmail(String email);

    // Also for finding by reset token
    Optional<UserAccount> findByResetToken(String resetToken);
    
    // Find users by role
    List<UserAccount> findByUserRole(String userRole);
    
    // Find users by company code
    @Query("SELECT u FROM UserAccount u WHERE u.company.companyCode = :companyCode")
    List<UserAccount> findByCompanyCode(@Param("companyCode") String companyCode);
    
    // Find users by role and company code
    @Query("SELECT u FROM UserAccount u WHERE u.userRole = :role AND u.company.companyCode = :companyCode")
    List<UserAccount> findByUserRoleAndCompanyCode(@Param("role") String role, @Param("companyCode") String companyCode);
    
    // Find business managers and testers by company code
    @Query("SELECT u FROM UserAccount u WHERE u.company.companyCode = :companyCode AND (u.userRole = 'BUSINESS_MANAGER' OR u.userRole = 'TESTER' OR u.userRole = 'QA_TESTER')")
    List<UserAccount> findBusinessManagersAndTestersByCompanyCode(@Param("companyCode") String companyCode);
    
    // Find user by firstName and lastName (case-insensitive)
    @Query("SELECT u FROM UserAccount u WHERE LOWER(TRIM(u.firstName)) = LOWER(TRIM(:firstName)) AND LOWER(TRIM(u.lastName)) = LOWER(TRIM(:lastName))")
    Optional<UserAccount> findByFirstNameAndLastName(@Param("firstName") String firstName, @Param("lastName") String lastName);
    
    // Find user by full name (handles "firstName lastName" format)
    @Query("SELECT u FROM UserAccount u WHERE LOWER(CONCAT(TRIM(u.firstName), ' ', TRIM(u.lastName))) = LOWER(TRIM(:fullName))")
    Optional<UserAccount> findByFullName(@Param("fullName") String fullName);

    // Find fallback users by firstName, lastName, partial userId, or partial email
    @Query("SELECT u FROM UserAccount u WHERE LOWER(u.firstName) = LOWER(:trimmed) " +
           "OR LOWER(u.lastName) = LOWER(:trimmed) " +
           "OR LOWER(u.userId) LIKE LOWER(CONCAT('%', :trimmed, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :trimmed, '%'))")
    List<UserAccount> findSystemFallbackUsers(@Param("trimmed") String trimmed);

}


