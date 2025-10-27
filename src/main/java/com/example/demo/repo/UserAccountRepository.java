package com.example.demo.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    UserAccount findByUserId(String userId);
    @Query(value = "SELECT MAX(UserID) FROM UserAccounts", nativeQuery = true)
    String findLastUserId();

    Optional<UserAccount> findByEmail(String email);

    // Also for finding by reset token
    Optional<UserAccount> findByResetToken(String resetToken);

}


