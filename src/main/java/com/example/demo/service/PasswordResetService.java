package com.example.demo.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demo.entity.UserAccount;
import com.example.demo.repo.UserAccountRepository;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.mail.javamail.MimeMessageHelper;

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

        // Dynamically build app URL
        String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String resetLink = appUrl + "/reset-password?token=" + token;

        // Send HTML Email
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("info@iwpl.org", "PerfectQA Security");
            helper.setTo(email);
            helper.setSubject("Password Reset Request - PerfectQA");

            String htmlContent = "<html><body style='font-family: Inter, Arial, sans-serif; background-color: #f4f7f9; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.05); border: 1px solid #e2e8f0;'>" +
                "<div style='background-color: #0f172a; color: #ffffff; padding: 40px; text-align: center;'>" +
                "<h1 style='margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.025em;'>PerfectQA</h1>" +
                "<p style='margin: 10px 0 0; opacity: 0.8; font-size: 16px;'>Security Service</p>" +
                "</div>" +
                "<div style='padding: 40px; text-align: center;'>" +
                "<h2 style='color: #1e293b; font-size: 22px; margin-bottom: 20px;'>Reset Your Password</h2>" +
                "<p style='font-size: 16px; color: #475569; line-height: 1.6;'>We received a request to reset your password. Click the button below to choose a new one. This link will expire shortly for your security.</p>" +
                "<div style='margin: 35px 0;'>" +
                "<a href='" + resetLink + "' style='background-color: #3b82f6; color: #ffffff; padding: 16px 32px; border-radius: 12px; text-decoration: none; font-weight: 700; font-size: 16px; display: inline-block; box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.2);'>Reset Password</a>" +
                "</div>" +
                "<p style='font-size: 14px; color: #64748b; margin-top: 30px;'>If you didn't request this, you can safely ignore this email.</p>" +
                "</div>" +
                "<div style='background-color: #f8fafc; padding: 25px; text-align: center; border-top: 1px solid #e2e8f0;'>" +
                "<p style='margin: 0; font-size: 13px; color: #94a3b8;'>&copy; 2026 PerfectQA - Intelligent Testing Platform.<br>IWPL Inc.</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("=== PASSWORD RESET EMAIL SENT TO: " + email + " ===");
        } catch (Exception e) {
            System.out.println("=== ERROR: Failed to send password reset email ===");
            e.printStackTrace();
            throw new Exception("Failed to send reset email. Please try again later.");
        }
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
