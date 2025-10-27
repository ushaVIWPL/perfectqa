package com.example.demo.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.example.demo.entity.UserAccount;
import com.example.demo.repo.UserAccountRepository;

@Service
public class UserAccountService {

    @Autowired
    private UserAccountRepository repo;

    // Save user and auto-generate UserID & Password
    public UserAccount createUser(UserAccount user) {
        // Check if email already exists
        if (repo.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists. Please use another email.");
        }

        // Generate UserID
        String userId = generateUserId(user);

        // Check if userId already exists
        UserAccount existingUser = repo.findByUserId(userId);
        if (existingUser != null) {
            throw new IllegalArgumentException("UserID '" + userId + "' already exists!");
        }

        // Set userId and password
        user.setUserId(userId);  
        user.setPassword(generatePassword(user.getCompanyName())); 

        // Save the user and handle any DataIntegrityViolation
        try {
            return repo.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Email already exists. Please use another email.");
        } catch (Exception e) {
            throw new IllegalArgumentException("An unexpected error occurred while creating the user.");
        }
    }



    // UserID: IWPL01, IWPL02...
 // Generate UserID based on FirstName + LastName
    public String generateUserId(UserAccount user) {
        String firstName = user.getFirstName();
        String lastName = user.getLastName();

        if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
            throw new IllegalArgumentException("First name and Last name cannot be empty");
        }

        // First letter of first name + last name
        String userId = firstName.substring(0, 1).toLowerCase() + lastName.toLowerCase();

        return userId;
    }


    // Password: Based on Company Name, e.g., first 3 letters + random 3 digits
    public String generatePassword(String companyName) {
        String prefix = companyName.length() >= 3 ? companyName.substring(0, 3).toUpperCase() : companyName.toUpperCase();
        int randomNum = (int) (Math.random() * 900) + 100; // random 3-digit number
        return prefix + randomNum;
    }
    
    public List<UserAccount>getAllUsers(){
    	return repo.findAll();
    }
    
    public UserAccount findByUserId(String userId) {
        return repo.findByUserId(userId);
    }

}


