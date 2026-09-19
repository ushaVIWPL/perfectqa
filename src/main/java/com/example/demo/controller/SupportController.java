package com.example.demo.controller;

import com.example.demo.entity.SupportRequest;
import com.example.demo.service.SupportRequestService;
import com.example.demo.util.RoleAccessUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/support")
public class SupportController {

    private final SupportRequestService supportRequestService;

    public SupportController(SupportRequestService supportRequestService) {
        this.supportRequestService = supportRequestService;
    }

    // Public support form page (accessible before login)
    @GetMapping("/request")
    public String showSupportForm(Model model) {
        model.addAttribute("supportRequest", new SupportRequest());
        return "support-form";
    }

    // Submit support request (public)
    @PostMapping("/submit")
    public String submitSupportRequest(@ModelAttribute SupportRequest supportRequest,
                                      RedirectAttributes redirectAttributes) {
        try {
            supportRequestService.createSupportRequest(supportRequest);
            redirectAttributes.addFlashAttribute("success", 
                "Thank you! Your support request has been submitted. We will contact you soon.");
            return "redirect:/support/request";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to submit support request: " + e.getMessage());
            return "redirect:/support/request";
        }
    }

    // Admin: View all support requests
    @GetMapping("/admin/requests")
    public String viewAllRequests(HttpSession session, Model model,
                                  @RequestParam(required = false) String status) {
        // Check if user is Admin
        if (!RoleAccessUtil.isAdmin(session)) {
            model.addAttribute("error", "Access denied. This page is only for Administrators.");
            return "redirect:/Menu";
        }

        List<SupportRequest> requests;
        if (status != null && !status.isEmpty()) {
            requests = supportRequestService.getRequestsByStatus(status);
        } else {
            requests = supportRequestService.getAllRequests();
        }

        long pendingCount = supportRequestService.countPendingRequests();
        
        model.addAttribute("requests", requests);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("selectedStatus", status != null ? status : "ALL");
        
        return "admin-support-requests";
    }

    // Admin: Mark request as contacted
    @PostMapping("/admin/contact/{id}")
    public String markAsContacted(@PathVariable Long id,
                                 @RequestParam(required = false) String adminNotes,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!RoleAccessUtil.isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Access denied.");
            return "redirect:/Menu";
        }

        String contactedBy = (String) session.getAttribute("username");
        if (contactedBy == null) {
            contactedBy = "Admin";
        }

        try {
            supportRequestService.markAsContacted(id, contactedBy, adminNotes);
            redirectAttributes.addFlashAttribute("success", "Support request marked as contacted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update request: " + e.getMessage());
        }

        return "redirect:/support/admin/requests";
    }

    // Admin: Update request status
    @PostMapping("/admin/update-status/{id}")
    public String updateStatus(@PathVariable Long id,
                              @RequestParam String status,
                              @RequestParam(required = false) String adminNotes,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!RoleAccessUtil.isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Access denied.");
            return "redirect:/Menu";
        }

        try {
            supportRequestService.updateStatus(id, status, adminNotes);
            redirectAttributes.addFlashAttribute("success", "Support request status updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update status: " + e.getMessage());
        }

        return "redirect:/support/admin/requests";
    }

    // Admin: View single request details
    @GetMapping("/admin/view/{id}")
    public String viewRequest(@PathVariable Long id, HttpSession session, Model model) {
        if (!RoleAccessUtil.isAdmin(session)) {
            model.addAttribute("error", "Access denied.");
            return "redirect:/Menu";
        }

        SupportRequest request = supportRequestService.getById(id)
                .orElseThrow(() -> new RuntimeException("Support request not found"));

        model.addAttribute("request", request);
        return "support-request-detail";
    }
}

