package com.example.demo.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.CountryRef;
import com.example.demo.entity.UserAccount;
import com.example.demo.repo.CountryRefRepository;
import com.example.demo.service.RegistrationService;
import com.example.demo.service.UserAccountService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping
public class UserAccountController {

	private final UserAccountService service;
    private final RegistrationService registrationService;
    private final CountryRefRepository countryRefRepository;

    

    
    @Autowired
    public UserAccountController(UserAccountService service,
                                 RegistrationService registrationService,
                                 CountryRefRepository countryRefRepository) {
        this.service = service;
        this.registrationService = registrationService;
        this.countryRefRepository = countryRefRepository;
    }

    @GetMapping("/users")
    public String getAllUsers(Model model) {
        List<UserAccount> users = service.getAllUsers(); 
        model.addAttribute("users", users); // passing the list to Thymeleaf
        return "admin"; // Thymeleaf template
    }



    
	
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        UserAccount user = new UserAccount();
        List<CountryRef> countries = registrationService.getCountries();

        model.addAttribute("user", user);
        model.addAttribute("countries", countries);
        return "registrationForm"; // matches the HTML page you posted
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute UserAccount user, Model model) {
        try {
            if (user.getUserRole() == null) {
                user.setUserRole("USER"); // default role
            }
            

            UserAccount createdUser = service.createUser(user);
            model.addAttribute("user", createdUser);
            model.addAttribute("success", "User registered successfully!"); // optional
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            // Important: Add a 'user' object so Thymeleaf can bind the form
            model.addAttribute("user", user);
        }

        // Always add countries list
        model.addAttribute("countries", registrationService.getCountries());
        return "registrationForm";
    }



    @GetMapping("/all")
    @ResponseBody
    public List<UserAccount> getAllUsersJson() {
        // Fetch all users
        return service.getAllUsers();
    }
	/*
	 * @PostMapping("/create") public String createUser(@ModelAttribute UserAccount
	 * user, Model model) { UserAccount createdUser = service.createUser(user);
	 * model.addAttribute("user", createdUser); model.addAttribute("countries",
	 * registrationService.getCountries()); // add again so dropdown works after
	 * submission return "registrationForm"; }
	 */

  
    @PostMapping("/login")
    public String loginUser(@RequestParam("UserID") String userId,
                            @RequestParam("Password") String password,
                            HttpSession session,
                            Model model) {

        UserAccount user = service.findByUserId(userId);

        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("role", user.getUserRole());
            session.setAttribute("userId", user.getUserId());
            model.addAttribute("user", user);
            model.addAttribute("role", user.getUserRole());
            String fullName = user.getFirstName() + " " + user.getLastName();
            session.setAttribute("username", fullName);

            switch (user.getUserRole().toUpperCase()) {
                case "ADMIN":
                    return "admin";
                case "BUSINESS_MANAGER":
                case "TESTER":
                default:
                    return "qatestermenu";
            }
        } else {
            model.addAttribute("error", "Invalid UserID or Password!"); // ✅ error added
            return "loginForm"; // redirect back to login page
        }
    }


 


    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate(); // clear session
        redirectAttributes.addFlashAttribute("logoutMessage", "You have been logged out successfully!");
        return "redirect:/home";
    }

    
    
 
    
    
}
