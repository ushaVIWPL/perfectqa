package com.example.demo.service;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demo.entity.UserAccount;
import com.example.demo.repo.UserAccountRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class UserAccountService {

    @Autowired
    private UserAccountRepository repo;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private WhatsappService whatsappService;
    
    @Autowired
    private SmsService smsService;
    
    // Characters for password generation
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "!@#$%&*";
    private static final SecureRandom random = new SecureRandom();

    // Generate a 6-digit OTP
    public String generateOTP() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        return otp.toString();
    }

    // Save user and auto-generate UserID (email) & Password
    public UserAccount createUser(UserAccount user) {
        // Validate email
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        // Check if email already exists
        if (repo.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("A user with this email already exists");
        }
        
        // Username is the email
        String userId = user.getEmail();
        
        // Check if userId already exists
        UserAccount existingUser = repo.findByUserId(userId);
        if (existingUser != null) {
            throw new IllegalArgumentException("A user with this email already exists");
        }

        // Generate password with company name + numbers + symbols
        String generatedPassword = generatePassword(user.getCompanyName());
        
        // Set userId (email) and password
        user.setUserId(userId);
        user.setPassword(generatedPassword);

        // Save the user
        try {
            System.out.println("=== SAVING USER ===");
            System.out.println("UserId (Email): " + user.getUserId());
            System.out.println("Email: " + user.getEmail());
            System.out.println("UserRole: " + user.getUserRole());
            System.out.println("Company: " + user.getCompany());
            System.out.println("CompanyCode: " + (user.getCompany() != null ? user.getCompany().getCompanyCode() : "NULL"));
            System.out.println("FirstName: " + user.getFirstName());
            System.out.println("LastName: " + user.getLastName());
            System.out.println("Status: " + user.getStatus());
            
            UserAccount saved = repo.save(user);
            
            System.out.println("=== USER SAVED SUCCESSFULLY ===");
            System.out.println("Saved UserId: " + saved.getUserId());
            System.out.println("Saved UserRole: " + saved.getUserRole());
            System.out.println("Saved CompanyCode: " + saved.getCompanyCode());
            System.out.println("Saved Status: " + saved.getStatus());
            
            // Send welcome email with credentials
            sendWelcomeEmail(saved, generatedPassword);
            
            return saved;
        } catch (Exception e) {
            System.out.println("=== ERROR SAVING USER ===");
            e.printStackTrace();
            throw new IllegalArgumentException("Error creating user: " + e.getMessage());
        }
    }

    // Generate UserID based on Email
    public String generateUserId(UserAccount user) {
        String email = user.getEmail();
        
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        return email; // Email is the username
    }

    // Password: Company name prefix + random numbers + symbols
    // Example: IWPL@847!Qa
    public String generatePassword(String companyName) {
        StringBuilder password = new StringBuilder();
        
        // Company name prefix (first 4 characters uppercase)
        String prefix;
        if (companyName == null || companyName.isEmpty()) {
            prefix = "USER";
        } else {
            // Remove spaces and get first 4 characters
            String cleanName = companyName.replaceAll("\\s+", "");
            prefix = cleanName.length() >= 4 
                ? cleanName.substring(0, 4).toUpperCase() 
                : cleanName.toUpperCase();
        }
        password.append(prefix);
        
        // Add a symbol
        password.append(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));
        
        // Add 3 random numbers
        for (int i = 0; i < 3; i++) {
            password.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        
        // Add another symbol
        password.append(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));
        
        // Add 2 random letters (mix of upper and lower)
        password.append((char) ('A' + random.nextInt(26)));
        password.append((char) ('a' + random.nextInt(26)));
        
        return password.toString();
    }
    
    // Send welcome email with username and password
 // Send welcome email with username and password
    private void sendWelcomeEmail(UserAccount user, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("info@iwpl.org");
            message.setTo(user.getEmail());
            message.setSubject("Welcome to PerfectQA - Your Account Credentials");

            // Hardcoded login/reset URL
            String resetPasswordLink = "http://perfectqa.net/profile";

            String emailBody = String.format(
                "Dear %s %s,\n\n" +
                "Welcome to PerfectQA! Your account has been successfully created.\n\n" +
                "Here are your login credentials:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "Username: %s\n" +
                "Password: %s\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "Login URL: %s\n\n" +  // <-- added here
                "For security reasons, we recommend changing your password after your first login.\n\n" +
                "Your Role: %s\n" +
                "Company: %s\n\n" +
                "If you have any questions, please contact your administrator.\n\n" +
                "Best regards,\n" +
                "PerfectQA Team\n" +
                "IWPL Inc.",
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                password,
                resetPasswordLink,
                user.getUserRole(),
                user.getCompanyName() != null ? user.getCompanyName() : "N/A"
            );

            message.setText(emailBody);
            mailSender.send(message);

            System.out.println("=== WELCOME EMAIL SENT TO: " + user.getEmail() + " ===");

        } catch (Exception e) {
            System.out.println("=== WARNING: Failed to send welcome email ===");
            e.printStackTrace();
        }
    }

    
    public List<UserAccount> getAllUsers() {
        return repo.findAll();
    }
    
    public UserAccount findByUserId(String userId) {
        return repo.findByUserId(userId);
    }
    
    // Find user by email (for login)
    public UserAccount findByEmail(String email) {
        return repo.findByEmail(email).orElse(null);
    }
    
    // Get all business managers
    public List<UserAccount> getAllBusinessManagers() {
        return repo.findByUserRole("BUSINESS_MANAGER");
    }
    
    // Get users by company code
    public List<UserAccount> getUsersByCompanyCode(String companyCode) {
        return repo.findByCompanyCode(companyCode);
    }
    
    // Get business managers and testers by company code
    public List<UserAccount> getBusinessManagersAndTestersByCompanyCode(String companyCode) {
        return repo.findBusinessManagersAndTestersByCompanyCode(companyCode);
    }
    
    // Get users by role and company code
    public List<UserAccount> getUsersByRoleAndCompanyCode(String role, String companyCode) {
        return repo.findByUserRoleAndCompanyCode(role, companyCode);
    }
    
    // Update existing user (don't regenerate userId or password)
    public UserAccount updateUser(UserAccount user) {
        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID is required for update.");
        }
        
        // Check if user exists
        UserAccount existingUser = repo.findByUserId(user.getUserId());
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found with ID: " + user.getUserId());
        }
        
        // Preserve password if not provided
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(existingUser.getPassword());
        }
        
        // Preserve created date
        if (user.getCreatedDate() == null) {
            user.setCreatedDate(existingUser.getCreatedDate());
        }
        
        // Preserve email if not provided (email = userId)
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            user.setEmail(existingUser.getEmail());
        }
        
        // Set deactivated date if status changed to inactive
        if (user.getStatus() != null && user.getStatus() == 0 && 
            (existingUser.getStatus() == null || existingUser.getStatus() == 1)) {
            user.setDeactivatedDate(java.time.LocalDateTime.now());
        }
        
        // Clear deactivated date if reactivated
        if (user.getStatus() != null && user.getStatus() == 1) {
            user.setDeactivatedDate(null);
        }
        
        try {
            UserAccount updated = repo.save(user);
            System.out.println("=== USER UPDATED SUCCESSFULLY: " + user.getUserId() + " ===");
            return updated;
        } catch (Exception e) {
            System.out.println("=== ERROR UPDATING USER ===");
            e.printStackTrace();
            throw new IllegalArgumentException("Error updating user: " + e.getMessage());
        }
    }
    
    // Reset password and send email
    public void resetPasswordAndNotify(String email) {
        UserAccount user = repo.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        
        // Generate new password
        String newPassword = generatePassword(user.getCompanyName());
        user.setPassword(newPassword);
        repo.save(user);
        
        // Send email with new password
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("info@iwpl.org");
            message.setTo(email);
            message.setSubject("PerfectQA - Password Reset");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "Your password has been reset.\n\n" +
                "New Password: %s\n\n" +
                "Please login and change your password immediately.\n\n" +
                "Best regards,\n" +
                "PerfectQA Team",
                user.getFirstName(),
                newPassword
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
        } catch (Exception e) {
            System.out.println("=== WARNING: Failed to send password reset email ===");
            e.printStackTrace();
        }
    }
    
    // Change password (user's own password)
    public void changePassword(String userId, String newPassword) {
        UserAccount user = repo.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
        user.setPassword(newPassword);
        repo.save(user);
        
        System.out.println("=== PASSWORD CHANGED FOR USER: " + userId + " ===");
        
        // Send confirmation email
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("info@iwpl.org");
            message.setTo(user.getEmail());
            message.setSubject("PerfectQA - Password Changed");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "Your password has been successfully changed.\n\n" +
                "If you did not make this change, please contact your administrator immediately.\n\n" +
                "Best regards,\n" +
                "PerfectQA Team",
                user.getFirstName()
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
        } catch (Exception e) {
            System.out.println("=== WARNING: Failed to send password change confirmation email ===");
            e.printStackTrace();
        }
    }

    // Delete user
    public void deleteUser(String userId) {
        UserAccount user = repo.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        repo.delete(user);
    }


    // Send OTP to Email, WhatsApp, and SMS
    public void sendOtp(UserAccount user, String otp) {
        // 1. Always send Email OTP (existing code - keep as is)
        sendOtpEmail(user.getEmail(), otp);

        // 2. Also send WhatsApp OTP if number exists
        String whatsappNo = user.getWhatsappNo(); // field name in your entity
        if (whatsappNo != null && !whatsappNo.trim().isEmpty()) {
            boolean sent = whatsappService.sendOTP(whatsappNo.trim(), otp);
            if (sent) {
                System.out.println("WhatsApp OTP sent to: " + whatsappNo);
            } else {
                System.err.println("WhatsApp OTP failed for: " + whatsappNo);
            }
        } else {
            System.out.println("No WhatsApp number found for user: " + user.getEmail());
        }

        // 3. Send SMS OTP if phone number exists
        String phoneNo = user.getPhoneNo();
        if (phoneNo != null && !phoneNo.trim().isEmpty()) {
            boolean sent = smsService.sendOTP(phoneNo.trim(), otp);
            if (sent) {
                System.out.println("SMS OTP sent to: " + phoneNo);
            } else {
                System.err.println("SMS OTP failed for: " + phoneNo);
            }
        } else {
            System.out.println("No phone number found for user: " + user.getEmail());
        }
    }

    // Send OTP email with premium HTML format
    public void sendOtpEmail(String email, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("info@iwpl.org", "PerfectQA Security");
            helper.setTo(email);
            helper.setSubject("Your One-Time Password (OTP) for PerfectQA");

            String htmlContent = "<html><body style='font-family: Arial, sans-serif; background-color: #f4f7f9; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.1);'>" +
                "<div style='background-color: #0f172a; color: #ffffff; padding: 30px; text-align: center;'>" +
                "<h1 style='margin: 0; font-size: 24px;'>PerfectQA Security</h1>" +
                "</div>" +
                "<div style='padding: 40px; text-align: center;'>" +
                "<p style='font-size: 16px; color: #475569;'>A login attempt was made for your account. Please use the following One-Time Password (OTP) to complete your login:</p>" +
                "<div style='margin: 30px 0; padding: 20px; background-color: #f1f5f9; border-radius: 8px;'>" +
                "<span style='font-size: 36px; font-weight: 700; letter-spacing: 10px; color: #0f172a;'>" + otp + "</span>" +
                "</div>" +
                "<p style='font-size: 14px; color: #64748b;'>This OTP will expire in 5 minutes. If you did not attempt to login, please ignore this email or contact support.</p>" +
                "</div>" +
                "<div style='background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #e2e8f0;'>" +
                "<p style='margin: 0; font-size: 12px; color: #94a3b8;'>&copy; 2026 PerfectQA - IWPL Inc. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            System.out.println("=== OTP EMAIL SENT TO: " + email + " | OTP: " + otp + " ===");

        } catch (Exception e) {
            System.out.println("=== ERROR: Failed to send OTP email | OTP was: " + otp + " ===");
            e.printStackTrace();
        }
    }
}





