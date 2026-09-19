package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.ApplicationRef;
import com.example.demo.entity.ProjectRef;
import com.example.demo.repo.Applicationrefrepo;
import com.example.demo.repo.ProjectRefRepository;
import com.example.demo.util.RoleAccessUtil;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/application")
public class ApplicationRefController {

    @Autowired
    private Applicationrefrepo applicationRepo;

    @Autowired
    private ProjectRefRepository projectRepo;

    // Show all applications + project dropdown
    @GetMapping("/list")
    public String listApplications(
            @RequestParam(required = false) String search,
            Model model, 
            HttpSession session) {
        // Only Admin and Business Manager can manage applications
        if (!RoleAccessUtil.canManageProjects(session)) {
            model.addAttribute("error", "You do not have permission to view applications.");
            return "redirect:/Menu";
        }
        
        List<ApplicationRef> applications;
        List<ProjectRef> projects = projectRepo.findAll();
        String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
        
        // If search term is provided, search by company name or code
        if (search != null && !search.trim().isEmpty()) {
            applications = applicationRepo.findByCompanyNameOrCodeContainingIgnoreCase(search.trim());
        } else {
            applications = applicationRepo.findAll();
        }
        
        // Business Manager can only see applications of their company's projects
        if (RoleAccessUtil.isBusinessManager(session) && companyCode != null) {
            applications = applications.stream()
                .filter(app -> app.getProject() != null && 
                             app.getProject().getCompany() != null &&
                             app.getProject().getCompany().getCompanyCode().equals(companyCode))
                .collect(Collectors.toList());
            
            projects = projects.stream()
                .filter(p -> p.getCompany() != null && p.getCompany().getCompanyCode().equals(companyCode))
                .collect(Collectors.toList());
        }
        
        model.addAttribute("applications", applications);
        model.addAttribute("projects", projects);
        model.addAttribute("application", new ApplicationRef());
        model.addAttribute("searchTerm", search != null ? search : "");
        return "applicationref"; // Thymeleaf page
    }

    // Save or Update
    @PostMapping("/save")
    public String saveApplication(@RequestParam String appCode,
                                  @RequestParam String appName,
                                  @RequestParam String projectCode,
                                  @RequestParam(required = false) String description,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        // Only Admin and Business Manager can save applications
        if (!RoleAccessUtil.canManageProjects(session)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to save applications.");
            return "redirect:/Menu";
        }
        
        // Business Manager can only save applications for their company's projects
        String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
        if (RoleAccessUtil.isBusinessManager(session) && companyCode != null) {
            ProjectRef project = projectRepo.findByProjectCode(projectCode).orElse(null);
            if (project == null || project.getCompany() == null || 
                !project.getCompany().getCompanyCode().equals(companyCode)) {
                redirectAttributes.addFlashAttribute("error", "You can only add applications to projects of your company.");
                return "redirect:/application/list";
            }
        }

        try {
            // Get Project
            ProjectRef project = projectRepo.findByProjectCode(projectCode)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid project code: " + projectCode));

            // Check if this is a new application or update
            boolean isNew = !applicationRepo.existsById(appCode);

            // Fetch existing or create new
            ApplicationRef app = applicationRepo.findById(appCode).orElse(new ApplicationRef());
            app.setAppCode(appCode);
            app.setAppName(appName);
            app.setProject(project);
            app.setDescription(description); // ✅ Important

            applicationRepo.save(app);
            
            // Add success message
            if (isNew) {
                redirectAttributes.addFlashAttribute("success", "Application saved successfully!");
            } else {
                redirectAttributes.addFlashAttribute("success", "Application updated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving application: " + e.getMessage());
        }
        
        return "redirect:/application/list";
    }

    // Edit
    @GetMapping("/edit/{code}")
    public String editApplication(@PathVariable String code, Model model, HttpSession session) {
        // Only Admin and Business Manager can edit applications
        if (!RoleAccessUtil.canManageProjects(session)) {
            model.addAttribute("error", "You do not have permission to edit applications.");
            return "redirect:/Menu";
        }
        
        ApplicationRef app = applicationRepo.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid application code: " + code));
        
        // Business Manager can only edit applications of their company
        String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
        if (RoleAccessUtil.isBusinessManager(session) && companyCode != null) {
            if (app.getProject() == null || app.getProject().getCompany() == null ||
                !app.getProject().getCompany().getCompanyCode().equals(companyCode)) {
                model.addAttribute("error", "You can only edit applications of your company.");
                return "redirect:/application/list";
            }
        }

        model.addAttribute("application", app);
        model.addAttribute("applications", applicationRepo.findAll());
        
        List<ProjectRef> projects = projectRepo.findAll();
        if (RoleAccessUtil.isBusinessManager(session) && companyCode != null) {
            projects = projects.stream()
                .filter(p -> p.getCompany() != null && p.getCompany().getCompanyCode().equals(companyCode))
                .collect(Collectors.toList());
        }
        model.addAttribute("projects", projects);
        return "applicationref";
    }

    // Delete
    @GetMapping("/delete/{code}")
    public String deleteApplication(@PathVariable String code, HttpSession session, Model model) {
        // Only Admin and Business Manager can delete applications
        if (!RoleAccessUtil.canManageProjects(session)) {
            model.addAttribute("error", "You do not have permission to delete applications.");
            return "redirect:/Menu";
        }
        
        ApplicationRef app = applicationRepo.findById(code).orElse(null);
        if (app != null) {
            // Business Manager can only delete applications of their company
            String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
            if (RoleAccessUtil.isBusinessManager(session) && companyCode != null) {
                if (app.getProject() == null || app.getProject().getCompany() == null ||
                    !app.getProject().getCompany().getCompanyCode().equals(companyCode)) {
                    model.addAttribute("error", "You can only delete applications of your company.");
                    return "redirect:/application/list";
                }
            }
        }
        
        applicationRepo.deleteById(code);
        return "redirect:/application/list";
    }
    
    // ============= API ENDPOINTS FOR ADMIN FILTERING =============
    
    // Get all applications as JSON (Admin only)
    @GetMapping("/api/all")
    @ResponseBody
    public List<Map<String, Object>> getAllApplicationsJson(HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return List.of();
        }
        return applicationRepo.findAll().stream()
            .map(this::applicationToMap)
            .collect(Collectors.toList());
    }
    
    // Get applications filtered by company code (Admin only)
    @GetMapping("/api/by-company")
    @ResponseBody
    public List<Map<String, Object>> getApplicationsByCompany(
            @RequestParam(required = false) String companyCode,
            HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return List.of();
        }
        
        List<ApplicationRef> applications;
        if (companyCode == null || companyCode.isEmpty() || "ALL".equalsIgnoreCase(companyCode)) {
            applications = applicationRepo.findAll();
        } else {
            applications = applicationRepo.findByCompanyCode(companyCode);
        }
        
        return applications.stream()
            .map(this::applicationToMap)
            .collect(Collectors.toList());
    }
    
    // Helper method to convert ApplicationRef to Map for JSON
    private Map<String, Object> applicationToMap(ApplicationRef app) {
        Map<String, Object> map = new HashMap<>();
        map.put("appCode", app.getAppCode());
        map.put("appName", app.getAppName());
        map.put("description", app.getDescription());
        if (app.getProject() != null) {
            map.put("projectCode", app.getProject().getProjectCode());
            map.put("projectName", app.getProject().getProjectName());
            if (app.getProject().getCompany() != null) {
                map.put("companyCode", app.getProject().getCompany().getCompanyCode());
                map.put("companyName", app.getProject().getCompany().getCompanyName());
            }
        }
        return map;
    }
}
