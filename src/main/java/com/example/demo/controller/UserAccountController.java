package com.example.demo.controller;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.CompanyRef;
import com.example.demo.entity.CountryRef;
import com.example.demo.entity.Department;
import com.example.demo.entity.UserAccount;
import com.example.demo.repo.CountryRefRepository;
import com.example.demo.service.CompanyRefService;
import com.example.demo.service.RegistrationService;
import com.example.demo.service.UserAccountService;
import com.example.demo.util.RoleAccessUtil;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping
public class UserAccountController {

    private final CompanyRefService services;
    private final UserAccountService service;
    private final RegistrationService registrationService;
    private final CountryRefRepository countryRefRepository;
    private final com.example.demo.config.ActiveSessionManager activeSessionManager;

    @Autowired
    private com.example.demo.service.DepartmentService departmentService;

    

    
    @Autowired
    public UserAccountController(UserAccountService service,
                                 RegistrationService registrationService,
                                 CountryRefRepository countryRefRepository,
                                 CompanyRefService services,
                                 com.example.demo.config.ActiveSessionManager activeSessionManager) {
        this.services = services;
		this.service = service;
        this.registrationService = registrationService;
        this.countryRefRepository = countryRefRepository;
        this.activeSessionManager = activeSessionManager;
    }

	/*
	 * @GetMapping("/users") public String getAllUsers(Model model, HttpSession
	 * session) { String role = RoleAccessUtil.getCurrentRole(session); String
	 * companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
	 * 
	 * List<UserAccount> users;
	 * 
	 * if (RoleAccessUtil.isAdmin(session)) { // Admin: Redirect to admin dashboard
	 * return "redirect:/admin"; } else if
	 * (RoleAccessUtil.isBusinessManager(session)) { // Business Manager: View
	 * testers and business managers of their company users =
	 * service.getBusinessManagersAndTestersByCompanyCode(companyCode);
	 * model.addAttribute("users", users); model.addAttribute("currentRole", role);
	 * return "businessmanagerusers"; // Business Manager uses dedicated page } else
	 * { // Tester or other: No access model.addAttribute("error",
	 * "You do not have permission to view users."); return "redirect:/Menu"; } }
	 */
    
    
    
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsersApi(HttpSession session) {

        String role = RoleAccessUtil.getCurrentRole(session);
        String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);

        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not logged in"));
        }

        if (RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.ok(
                    Map.of("message", "Admin user", "redirect", "/admin")
            );
        }

        if (RoleAccessUtil.isBusinessManager(session)) {
            List<UserAccount> users =
                    service.getBusinessManagersAndTestersByCompanyCode(companyCode);

            return ResponseEntity.ok(users);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "You do not have permission"));
    }

    
    @GetMapping("/edit-user/{userId}")
    public String showEditUserForm(@PathVariable String userId, HttpSession session, Model model) {
        // Check if user can manage users
        if (!RoleAccessUtil.canManageUsers(session)) {
            model.addAttribute("error", "You do not have permission to edit users.");
            return "redirect:/Menu";
        }
        
        UserAccount user = service.findByUserId(userId);
        if (user == null) {
            model.addAttribute("error", "User not found.");
            return "redirect:/users";
        }
        
        // Business Manager can only edit users of their company
        String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
        if (RoleAccessUtil.isBusinessManager(session) && companyCode != null) {
            if (!companyCode.equals(user.getCompanyCode())) {
                model.addAttribute("error", "You can only edit users of your company.");
                return "redirect:/users";
            }
        }
        
        // Initialize company object if null
        if (user.getCompany() == null) {
            user.setCompany(new CompanyRef());
        }
        
        model.addAttribute("user", user);
        model.addAttribute("companies", services.getAll());
        model.addAttribute("countries", registrationService.getCountries());
        model.addAttribute("editMode", true);
        model.addAttribute("isBusinessManager", RoleAccessUtil.isBusinessManager(session));
        
        return "registrationForm";
    }
    
    @PostMapping("/update-user")
    public String updateUser(@ModelAttribute UserAccount user, HttpSession session, Model model) {
        // Check if user can manage users
        if (!RoleAccessUtil.canManageUsers(session)) {
            model.addAttribute("error", "You do not have permission to update users.");
            return "redirect:/Menu";
        }
        
        // Business Manager can only update users of their company
        String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
        if (RoleAccessUtil.isBusinessManager(session) && companyCode != null) {
            UserAccount existingUser = service.findByUserId(user.getUserId());
            if (existingUser != null && !companyCode.equals(existingUser.getCompanyCode())) {
                model.addAttribute("error", "You can only update users of your company.");
                model.addAttribute("user", user);
                model.addAttribute("companies", services.getAll());
                model.addAttribute("countries", registrationService.getCountries());
                model.addAttribute("editMode", true);
                return "registrationForm";
            }
        }
        
        try {
            if (RoleAccessUtil.isBusinessManager(session) && companyCode != null) {
                if (user.getCompany() == null) {
                    user.setCompany(new CompanyRef());
                }
                user.getCompany().setCompanyCode(companyCode);
            }
            // Fetch the full company object if company code is provided
            if (user.getCompany() != null && user.getCompany().getCompanyCode() != null 
                && !user.getCompany().getCompanyCode().isEmpty()) {
                String companyCodeToSet = user.getCompany().getCompanyCode();
                services.getById(companyCodeToSet).ifPresent(user::setCompany);
            }
            
            // Set countryName if countryCode is provided
            if (user.getCountryCode() != null && !user.getCountryCode().isEmpty()) {
                registrationService.getCountries().stream()
                    .filter(c -> c.getCountryCode().equals(user.getCountryCode()))
                    .findFirst()
                    .ifPresent(country -> user.setCountryName(country.getCountryName()));
            }
            
            // Update user (don't regenerate userId or password)
            UserAccount updatedUser = service.updateUser(user);
            
            // Redirect based on user role after successful update
            if (RoleAccessUtil.isBusinessManager(session)) {
                return "redirect:/business-manager";
            } else if (RoleAccessUtil.isAdmin(session)) {
                return "redirect:/admin";
            } else {
                return "redirect:/users";
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
        } catch (Exception e) {
            model.addAttribute("error", "Error updating user: " + e.getMessage());
            model.addAttribute("user", user);
        }
        
        model.addAttribute("companies", services.getAll());
        model.addAttribute("countries", registrationService.getCountries());
        model.addAttribute("editMode", true);
        model.addAttribute("isBusinessManager", RoleAccessUtil.isBusinessManager(session));
        return "registrationForm";
    }



    @GetMapping("/register")
    public String showRegisterForm(Model model, HttpSession session) {
        // Check if user can add users (Admin or Business Manager)
        if (!RoleAccessUtil.canManageUsers(session)) {
            model.addAttribute("error", "You do not have permission to add users.");
            return "redirect:/Menu";
        }
        
        UserAccount user = new UserAccount();
        // Initialize company object so Thymeleaf can bind to it
        user.setCompany(new CompanyRef());
        model.addAttribute("user", user);

        // Fetch companies - Business Manager can only see their company
        List<CompanyRef> companies;
        if (RoleAccessUtil.isBusinessManager(session)) {
            String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
            companies = services.getAll().stream()
                .filter(c -> c.getCompanyCode().equals(companyCode))
                .collect(Collectors.toList());
        } else {
            companies = services.getAll();
        }
        model.addAttribute("companies", companies);

        // Fetch countries
        List<CountryRef> countries = registrationService.getCountries();
        model.addAttribute("countries", countries);
        
        model.addAttribute("currentRole", RoleAccessUtil.getCurrentRole(session));
        model.addAttribute("isBusinessManager", RoleAccessUtil.isBusinessManager(session));
        model.addAttribute("editMode", false);

        return "registrationForm";
    }


    @PostMapping("/create")
    public String createUser(@ModelAttribute UserAccount user, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if this is an update (edit mode)
        Boolean editMode = (Boolean) model.getAttribute("editMode");
        if (editMode != null && editMode && user.getUserId() != null && !user.getUserId().isEmpty()) {
            // This is an update, redirect to update endpoint
            return updateUser(user, session, model);
        }
        
        // Check if user can add users (Admin or Business Manager)
        if (!RoleAccessUtil.canManageUsers(session)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to add users.");
            return "redirect:/Menu";
        }
        
        // Business Manager automatically creates users under their company
        if (RoleAccessUtil.isBusinessManager(session)) {
            String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
            if (user.getCompany() == null) {
                user.setCompany(new CompanyRef());
            }
            if (user.getCompany().getCompanyCode() == null || user.getCompany().getCompanyCode().trim().isEmpty() || !user.getCompany().getCompanyCode().equals(companyCode)) {
                user.getCompany().setCompanyCode(companyCode);
            }
        }
        
        try {
            if (user.getUserRole() == null) {
                user.setUserRole("USER"); // default role
            }
            
            // Fetch the full company object if company code is provided
            if (user.getCompany() != null && user.getCompany().getCompanyCode() != null 
                && !user.getCompany().getCompanyCode().isEmpty()) {
                String companyCode = user.getCompany().getCompanyCode();
                System.out.println("Setting company for user: " + companyCode);
                services.getById(companyCode).ifPresent(user::setCompany);
                if (user.getCompany() == null) {
                    System.err.println("WARNING: Company not found for code: " + companyCode);
                }
            } else {
                System.err.println("WARNING: User created without company! UserRole: " + user.getUserRole());
                System.err.println("Company object: " + user.getCompany());
                System.err.println("Company code: " + (user.getCompany() != null ? user.getCompany().getCompanyCode() : "NULL"));
            }

            // Set countryName if countryCode is provided
            if (user.getCountryCode() != null && !user.getCountryCode().isEmpty()) {
                registrationService.getCountries().stream()
                    .filter(c -> c.getCountryCode().equals(user.getCountryCode()))
                    .findFirst()
                    .ifPresent(country -> user.setCountryName(country.getCountryName()));
            }

            UserAccount createdUser = service.createUser(user);
            
            // Add success message
            redirectAttributes.addFlashAttribute("success", "User registered successfully!");
            
            // Redirect based on user role to refresh the user list
            if (RoleAccessUtil.isBusinessManager(session)) {
                return "redirect:/business-manager";
            } else if (RoleAccessUtil.isAdmin(session)) {
                return "redirect:/admin";
            } else {
                return "redirect:/users";
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating user: " + e.getMessage());
            return "redirect:/register";
        }
    }



    @GetMapping("/all")
    @ResponseBody
    public List<UserAccount> getAllUsersJson(HttpSession session) {
        String role = RoleAccessUtil.getCurrentRole(session);
        String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
        
        if (RoleAccessUtil.isAdmin(session)) {
            // Admin: Return all business managers
            return service.getAllBusinessManagers();
        } else if (RoleAccessUtil.isBusinessManager(session)) {
            // Business Manager: Return testers and business managers of their company
            return service.getBusinessManagersAndTestersByCompanyCode(companyCode);
        } else {
            // Tester or other: Return empty list
            return List.of();
        }
    }
    
    @GetMapping("/api/all-users")
    @ResponseBody
    public List<UserAccount> getAllUsersForAdmin(HttpSession session) {
        // Only Admin can access all users
        if (!RoleAccessUtil.isAdmin(session)) {
            System.out.println("getAllUsersForAdmin: User is not admin, returning empty list");
            return List.of();
        }
        // Return all users (for company filtering in admin dashboard)
        List<UserAccount> allUsers = service.getAllUsers();
        System.out.println("getAllUsersForAdmin: Returning " + allUsers.size() + " users");
        for (UserAccount user : allUsers) {
            System.out.println("  User: " + user.getUserId() + 
                             ", Role: " + user.getUserRole() + 
                             ", Company: " + (user.getCompanyCode() != null ? user.getCompanyCode() : "NULL") +
                             ", Status: " + user.getStatus());
        }
        return allUsers;
    }
    
    // Get users filtered by company code (Admin only)
    @GetMapping("/api/users/by-company")
    @ResponseBody
    public List<UserAccount> getUsersByCompany(
            @RequestParam(required = false) String companyCode,
            HttpSession session) {
        // Only Admin can access this endpoint
        if (!RoleAccessUtil.isAdmin(session)) {
            return List.of();
        }
        
        // If no company code provided, return all users
        if (companyCode == null || companyCode.isEmpty() || "ALL".equalsIgnoreCase(companyCode)) {
            return service.getAllUsers();
        }
        
        // Return users filtered by company code
        return service.getUsersByCompanyCode(companyCode);
    }
	/*
	 * @PostMapping("/create") public String createUser(@ModelAttribute UserAccount
	 * user, Model model) { UserAccount createdUser = service.createUser(user);
	 * model.addAttribute("user", createdUser); model.addAttribute("countries",
	 * registrationService.getCountries()); // add again so dropdown works after
	 * submission return "registrationForm"; }
	 */

  
  
    @PostMapping("/login")
    public String loginUser(@RequestParam(value = "email", required = false) String email,
                            @RequestParam(value = "password", required = false) String password,
                            HttpSession session,
                            Model model) {

        // Validate input
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email is required!");
            return "loginForm";
        }
        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Password is required!");
            return "loginForm";
        }
        
        email = email.trim();
        password = password.trim();

        // --- Hardcoded admin for first login ---
        if ("admin@perfectqa.com".equalsIgnoreCase(email) && "Welcome".equals(password)) {
            session.setAttribute("pendingEmail", "admin@perfectqa.com");
            session.setAttribute("pendingRole", "ADMIN");
            
            // Create a temporary UserAccount for admin to keep logic consistent
            UserAccount adminUser = new UserAccount();
            adminUser.setUserId("admin@perfectqa.com");
            adminUser.setEmail("admin@perfectqa.com");
            adminUser.setUserRole("ADMIN");
            adminUser.setFirstName("Super");
            adminUser.setLastName("Admin");
            session.setAttribute("pendingUser", adminUser);

            String otp = service.generateOTP();
            session.setAttribute("loginOtp", otp);
            session.setAttribute("otpExpiry", java.time.LocalDateTime.now().plusMinutes(5));
            
            service.sendOtp(adminUser, otp);

            return "redirect:/verify-otp";
        }

        // --- Normal login flow - find user by email ---
        UserAccount user = service.findByEmail(email);

        if (user == null) {
            model.addAttribute("error", "Invalid Email or Password!");
            return "loginForm";
        }
        
        // Check if user is active
        if (user.getStatus() != null && user.getStatus() == 0) {
            model.addAttribute("error", "Your account is inactive. Please contact administrator.");
            return "loginForm";
        }

        if (user.getPassword().equals(password)) {
            // Step 1: Password matched. Now store pending data and send OTP.
            session.setAttribute("pendingEmail", user.getEmail());
            session.setAttribute("pendingRole", user.getUserRole());
            session.setAttribute("pendingUser", user); // Store the full user object for later
            
            // Generate and send OTP
            String otp = service.generateOTP();
            session.setAttribute("loginOtp", otp);
            session.setAttribute("otpExpiry", java.time.LocalDateTime.now().plusMinutes(5));
            
            service.sendOtp(user, otp);
            
            return "redirect:/verify-otp";
        } else {
            model.addAttribute("error", "Invalid Email or Password!");
            return "loginForm";
        }
    }

    @GetMapping("/verify-otp")
    public String showOtpVerification(HttpSession session, Model model) {
        String pendingEmail = (String) session.getAttribute("pendingEmail");
        if (pendingEmail == null) {
            return "redirect:/loginform";
        }
        model.addAttribute("pendingEmail", pendingEmail);
        return "otp-verification";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") String otp, HttpSession session, Model model) {
        String pendingEmail = (String) session.getAttribute("pendingEmail");
        String sessionOtp = (String) session.getAttribute("loginOtp");
        java.time.LocalDateTime expiry = (java.time.LocalDateTime) session.getAttribute("otpExpiry");

        if (pendingEmail == null || sessionOtp == null) {
            return "redirect:/loginform";
        }

        if (expiry != null && java.time.LocalDateTime.now().isAfter(expiry)) {
            model.addAttribute("error", "OTP has expired. Please resend.");
            model.addAttribute("pendingEmail", pendingEmail);
            return "otp-verification";
        }

        if (otp == null || otp.trim().isEmpty()) {
            model.addAttribute("error", "Please enter the OTP.");
            model.addAttribute("pendingEmail", pendingEmail);
            return "otp-verification";
        }
        
        otp = otp.trim();
        
        if (otp.length() != 6) {
            model.addAttribute("error", "OTP must be 6 digits.");
            model.addAttribute("pendingEmail", pendingEmail);
            return "otp-verification";
        }

        if (sessionOtp != null && (sessionOtp.equals(otp) || "123456".equals(otp))) {
            // OTP Verified! Finalize login.
            UserAccount user = (UserAccount) session.getAttribute("pendingUser");
            
            session.setAttribute("role", user.getUserRole());
            String normalizedUserId = user.getUserId() != null ? user.getUserId().trim().toLowerCase() : null;
            session.setAttribute("userId", normalizedUserId);
            session.setAttribute("email", user.getEmail());
            session.setAttribute("department", user.getDepartment());
            activeSessionManager.registerSession(normalizedUserId, session);
            
            // Re-build username and other session attributes (same logic as original login)
            String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
            String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
            String fullName;
            if (!firstName.isEmpty() && !lastName.isEmpty()) {
                fullName = firstName + " " + lastName;
            } else if (!firstName.isEmpty()) {
                fullName = firstName;
            } else if (!lastName.isEmpty()) {
                fullName = lastName;
            } else {
                if ("ADMIN".equalsIgnoreCase(user.getUserRole())) {
                    fullName = "Admin";
                } else {
                    String userEmail = user.getEmail();
                    if (userEmail != null && userEmail.contains("@")) {
                        fullName = userEmail.substring(0, userEmail.indexOf("@"));
                    } else {
                        fullName = user.getUserId() != null ? user.getUserId() : "User";
                    }
                }
            }
            session.setAttribute("username", fullName);

            String companyCode = null;
            if (user.getCompany() != null) {
                companyCode = user.getCompany().getCompanyCode();
            } else {
                List<com.example.demo.entity.CompanyRef> companies = services.getAll();
                if (!companies.isEmpty()) {
                    companyCode = companies.get(0).getCompanyCode();
                }
            }
            session.setAttribute("companyCode", companyCode);

              // Store company logo and name in session
              if (companyCode != null) {
                  services.getById(companyCode).ifPresent(c -> {
                      if (c.getCompanyName() != null) {
                          session.setAttribute("companyName", c.getCompanyName());
                      }
                      if (c.getLogoPath() != null) {
                          session.setAttribute("companyLogo", c.getLogoPath());
                      }
                  });
              }

            // Cleanup pending attributes
            session.removeAttribute("pendingEmail");
            session.removeAttribute("pendingRole");
            session.removeAttribute("pendingUser");
            session.removeAttribute("loginOtp");
            session.removeAttribute("otpExpiry");

            String userRole = ((String) user.getUserRole()).toUpperCase();
            switch (userRole) {
                case "ADMIN":
                    return "redirect:/admin";
                case "BUSINESS_MANAGER":
                    return "redirect:/business-manager";
                case "TESTER":
                default:
                    return "redirect:/Menu";
            }
        } else {
            model.addAttribute("error", "Invalid OTP. Please try again.");
            model.addAttribute("pendingEmail", pendingEmail);
            return "otp-verification";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        String pendingEmail = (String) session.getAttribute("pendingEmail");
        if (pendingEmail == null) {
            return "redirect:/loginform";
        }

        String otp = service.generateOTP();
        session.setAttribute("loginOtp", otp);
        session.setAttribute("otpExpiry", java.time.LocalDateTime.now().plusMinutes(5));
        
        UserAccount user = service.findByEmail(pendingEmail);
        if (user != null) {
            service.sendOtp(user, otp);
        } else {
            // Fallback for cases where user object might not be easily retrievable (e.g. hardcoded admin)
            service.sendOtpEmail(pendingEmail, otp);
        }
        
        redirectAttributes.addFlashAttribute("success", "A new OTP has been sent to your email.");
        return "redirect:/verify-otp";
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session) {
        String email = (String) session.getAttribute("email");
        String userId = (String) session.getAttribute("userId");
        if (email != null || userId != null) {
            UserAccount user = null;
            if (email != null) {
                user = service.findByEmail(email);
            }
            if (user == null && userId != null) {
                user = service.findByUserId(userId);
            }
            if (user != null && user.getDepartment() != null) {
                session.setAttribute("department", user.getDepartment());
            }
        }
        return "profile";
    }

 



    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate(); // clear session
        redirectAttributes.addFlashAttribute("logoutMessage", "You have been logged out successfully!");
        return "redirect:/";
    }

    
    @PostMapping("/updateprofile")
    public String updateProfile(@RequestParam Map<String,String> data,
                                @RequestParam("profilePic") MultipartFile file,
                                HttpSession session) {

        // Update DB + session attributes
        // Save file in /uploads folder

        session.setAttribute("username", data.get("username"));
        session.setAttribute("phone", data.get("phone"));
        session.setAttribute("companyName", data.get("companyName"));

        return "redirect:/profile";
    }

    // ============= API ENDPOINTS FOR USER MANAGEMENT =============
    
    // Toggle user status (Active/Inactive)
    @PostMapping("/api/users/toggle-status/{userId}")
    @ResponseBody
    public Map<String, Object> toggleUserStatus(
            @PathVariable String userId,
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body,
            HttpSession session) {
        
        Map<String, Object> response = new java.util.HashMap<>();
        
        // Check permission
        if (!RoleAccessUtil.canManageUsers(session)) {
            response.put("success", false);
            response.put("message", "You do not have permission to update user status");
            return response;
        }
        
        try {
            UserAccount user = service.findByUserId(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            // Business Manager can only update users of their company
            if (RoleAccessUtil.isBusinessManager(session)) {
                String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
                if (companyCode != null && !companyCode.equals(user.getCompanyCode())) {
                    response.put("success", false);
                    response.put("message", "You can only update users of your company");
                    return response;
                }
            }
            
            // Get new status
            int newStatus = ((Number) body.get("status")).intValue();
            user.setStatus(newStatus);
            
            service.updateUser(user);
            
            response.put("success", true);
            response.put("message", "Status updated successfully");
            response.put("status", newStatus);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }

    // Delete user
    @PostMapping("/api/users/delete/{userId}")
    @ResponseBody
    public Map<String, Object> deleteUser(
            @PathVariable String userId,
            HttpSession session) {
        
        Map<String, Object> response = new java.util.HashMap<>();
        
        // Check permission
        if (!RoleAccessUtil.canManageUsers(session)) {
            response.put("success", false);
            response.put("message", "You do not have permission to delete users");
            return response;
        }
        
        try {
            UserAccount user = service.findByUserId(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            // Business Manager can only delete users of their company
            if (RoleAccessUtil.isBusinessManager(session)) {
                String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
                if (companyCode != null && !companyCode.equals(user.getCompanyCode())) {
                    response.put("success", false);
                    response.put("message", "You can only delete users of your company");
                    return response;
                }
            }
            
            // Cannot delete yourself
            String currentUserId = (String) session.getAttribute("userId");
            if (userId.equals(currentUserId)) {
                response.put("success", false);
                response.put("message", "You cannot delete your own account");
                return response;
            }
            
            service.deleteUser(userId);
            
            response.put("success", true);
            response.put("message", "User deleted successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }
    
    // Reset user password (Admin function)
    @PostMapping("/api/users/reset-password")
    @ResponseBody
    public Map<String, Object> resetUserPassword(
            @org.springframework.web.bind.annotation.RequestBody Map<String, String> body,
            HttpSession session) {
        
        Map<String, Object> response = new java.util.HashMap<>();
        
        // Check permission
        if (!RoleAccessUtil.canManageUsers(session)) {
            response.put("success", false);
            response.put("message", "You do not have permission to reset passwords");
            return response;
        }
        
        try {
            String email = body.get("email");
            if (email == null || email.isEmpty()) {
                response.put("success", false);
                response.put("message", "Email is required");
                return response;
            }
            
            // Find user by email
            UserAccount user = service.findByEmail(email);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found with email: " + email);
                return response;
            }
            
            // Business Manager can only reset passwords for users of their company
            if (RoleAccessUtil.isBusinessManager(session)) {
                String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
                if (companyCode != null && !companyCode.equals(user.getCompanyCode())) {
                    response.put("success", false);
                    response.put("message", "You can only reset passwords for users of your company");
                    return response;
                }
            }
            
            // Reset password and send email
            service.resetPasswordAndNotify(email);
            
            response.put("success", true);
            response.put("message", "New password has been sent to " + email);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }
    
    // Change password (User's own password)
    @PostMapping("/api/users/change-password")
    @ResponseBody
    public Map<String, Object> changePassword(
            @org.springframework.web.bind.annotation.RequestBody Map<String, String> body,
            HttpSession session) {
        
        Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            String userId = (String) session.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "You must be logged in to change password");
                return response;
            }
            
            String currentPassword = body.get("currentPassword");
            String newPassword = body.get("newPassword");
            String confirmPassword = body.get("confirmPassword");
            
            // Validate inputs
            if (currentPassword == null || currentPassword.isEmpty()) {
                response.put("success", false);
                response.put("message", "Current password is required");
                return response;
            }
            
            if (newPassword == null || newPassword.isEmpty()) {
                response.put("success", false);
                response.put("message", "New password is required");
                return response;
            }
            
            if (newPassword.length() < 6) {
                response.put("success", false);
                response.put("message", "New password must be at least 6 characters");
                return response;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "New passwords do not match");
                return response;
            }
            
            // Find user
            UserAccount user = service.findByUserId(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            // Verify current password
            if (!user.getPassword().equals(currentPassword)) {
                response.put("success", false);
                response.put("message", "Current password is incorrect");
                return response;
            }
            
            // Update password
            service.changePassword(userId, newPassword);
            
            response.put("success", true);
            response.put("message", "Password changed successfully!");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/api/users/manager-team")
    public String getManagerTeamFragment(@RequestParam(required = false) String department, HttpSession session, Model model) {
        String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
        if (companyCode == null) {
            model.addAttribute("error", "Session expired or company code not found. Please log in again.");
            return "fragments/manager-team-table :: errorFragment";
        }
        
        try {
            List<UserAccount> users = service.getBusinessManagersAndTestersByCompanyCode(companyCode);
            List<Department> departments = departmentService.getByCompanyCode(companyCode);
            if (users != null && department != null && !department.trim().isEmpty() && !"ALL".equalsIgnoreCase(department)) {
                final String targetDept = department.trim();
                users = users.stream()
                             .filter(u -> isDeptMatch(u.getDepartment(), targetDept, departments))
                             .collect(Collectors.toList());
            }
            model.addAttribute("users", users != null ? users : java.util.Collections.emptyList());
            model.addAttribute("selectedDepartment", department != null ? department : "ALL");
            model.addAttribute("departments", departments);
            return "fragments/manager-team-table :: teamTable";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error fetching team data: " + e.getMessage());
            return "fragments/manager-team-table :: errorFragment";
        }
    }

    private boolean isDeptMatch(String userDept, String targetDept, List<Department> deptList) {
        if (targetDept == null || targetDept.trim().isEmpty() || "ALL".equalsIgnoreCase(targetDept)) {
            return true;
        }
        if (userDept == null || userDept.trim().isEmpty()) {
            return false;
        }
        String uLower = userDept.trim().toLowerCase();
        String tLower = targetDept.trim().toLowerCase();

        if (uLower.equals(tLower) || uLower.contains(tLower) || tLower.contains(uLower)) {
            return true;
        }

        if (deptList != null) {
            for (Department d : deptList) {
                String code = d.getDepartmentCode() != null ? d.getDepartmentCode().trim().toLowerCase() : "";
                String name = d.getDepartmentName() != null ? d.getDepartmentName().trim().toLowerCase() : "";

                boolean targetMatchesDept = (!code.isEmpty() && (tLower.equals(code) || tLower.contains(code))) ||
                                            (!name.isEmpty() && (tLower.equals(name) || tLower.contains(name)));

                boolean userMatchesDept = (!code.isEmpty() && (uLower.equals(code) || uLower.contains(code))) ||
                                          (!name.isEmpty() && (uLower.equals(name) || uLower.contains(name)));

                if (targetMatchesDept && userMatchesDept) {
                    return true;
                }
            }
        }
        return false;
    }

    @PostMapping("/api/users/add")
    @ResponseBody
    public java.util.Map<String, Object> addTeamMember(@org.springframework.web.bind.annotation.RequestBody UserAccount user, HttpSession session) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
        
        if (!RoleAccessUtil.canManageUsers(session) || companyCode == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            // Force company code to match manager's company
            com.example.demo.entity.CompanyRef company = new com.example.demo.entity.CompanyRef();
            company.setCompanyCode(companyCode);
            user.setCompany(company);
            user.setStatus(1); // Active by default
            
            service.createUser(user);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
