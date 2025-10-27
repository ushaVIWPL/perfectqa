package com.example.demo.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demo.entity.UserAccount;
import com.example.demo.repo.UserAccountRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class PasswordResetService {

    @Autowired
    private UserAccountRepository userRepo;

    @Autowired
    private JavaMailSender mailSender;

   


    // Step 1: Generate reset token and send reset email
    public void sendResetLink(String email, HttpServletRequest request) throws Exception {
        Optional<UserAccount> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new Exception("No user found with email: " + email);
        }

        UserAccount user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        userRepo.save(user);

        // Dynamically build app URL (works both locally and on production)
        String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        String resetLink = appUrl + "/reset-password?token=" + token;

        // Email message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ushavarma@iwpl.org");
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click the link below:\n" + resetLink);

        mailSender.send(message);
    }


    // Step 2: Reset password using token
	/*
	 * public void resetPassword(String token, String newPassword) throws Exception
	 * { Optional<UserAccount> userOpt = userRepo.findByResetToken(token); if
	 * (userOpt.isEmpty()) { throw new Exception("Invalid or expired reset token");
	 * }
	 * 
	 * UserAccount user = userOpt.get(); user.setResetToken(null);
	 * userRepo.save(user); }
	 */
    
    
    
    public void resetPassword(String token, String newPassword) {
        UserAccount user = userRepo.findByResetToken(token)
                                   .orElseThrow(() -> new RuntimeException("Invalid password reset token"));

        // Set new password
        user.setPassword(newPassword); // or passwordEncoder.encode(newPassword)

        // Clear the reset token
        user.setResetToken(null);

        // Update the database
        userRepo.save(user);
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
