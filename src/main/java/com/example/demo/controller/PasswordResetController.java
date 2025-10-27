package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.service.PasswordResetService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class PasswordResetController {

    @Autowired
    private PasswordResetService resetService;

    // Step 1: Show forgot password form
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    // Step 2: Handle email submission
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        HttpServletRequest request,
                                        Model model) {
        try {
        	resetService.sendResetLink(email, request);
            model.addAttribute("message", "A password reset link has been sent to your email.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "forgot-password";
    }


    // Step 3: Show reset password form
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    // Step 4: Handle password reset submission
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       Model model) {
        try {
            resetService.resetPassword(token, password);
            model.addAttribute("message", "Password reset successfully! You can now log in.");
            return "reset-password"; // ✅ stay here temporarily to show SweetAlert
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "reset-password";
        }
    }
}

