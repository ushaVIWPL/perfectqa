package com.example.demo.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MaxUploadSizeExceededException.class, MultipartException.class})
    public String handleFileSizeException(Exception ex, HttpServletRequest request, Model model, 
                                         RedirectAttributes redirectAttributes, HttpSession session) {
        String errorMessage = "File size exceeds the maximum allowed limit of 10MB. Please upload a smaller image.";
        
        // Check if the exception message contains size information for more specific error
        if (ex.getMessage() != null && ex.getMessage().contains("exceed")) {
            errorMessage = "File size exceeds the maximum allowed limit of 10MB. Please upload a smaller image.";
        }
        
        // Determine which page to return to based on the request URL
        String requestURI = request.getRequestURI();
        
        if (requestURI != null) {
            if (requestURI.contains("/savetestheader")) {
                // Return to test case header form with minimal model attributes
                model.addAttribute("error", errorMessage);
                model.addAttribute("testCaseHeader", new com.example.demo.entity.TestCaseHeader());
                model.addAttribute("editMode", false);
                model.addAttribute("imagePaths", new java.util.ArrayList<String>());
                model.addAttribute("existingImageCount", 0);
                return "addtestcaseheader";
            } else if (requestURI.contains("/save-transaction")) {
                // Return to transaction form with minimal model attributes
                model.addAttribute("error", errorMessage);
                model.addAttribute("transaction", new com.example.demo.entity.TestCaseTransaction());
                model.addAttribute("testCaseHeader", null);
                model.addAttribute("editMode", false);
                model.addAttribute("imagePaths", new java.util.ArrayList<String>());
                model.addAttribute("existingImageCount", 0);
                return "addtransactionform";
            }
        }
        
        // Default: redirect with flash attribute
        redirectAttributes.addFlashAttribute("error", errorMessage);
        return "redirect:/Menu";
    }
}

