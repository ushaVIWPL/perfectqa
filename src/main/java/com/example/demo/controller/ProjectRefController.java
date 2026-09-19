package com.example.demo.controller;

import com.example.demo.entity.CompanyRef;
import com.example.demo.entity.ProjectRef;
import com.example.demo.service.CompanyRefService;
import com.example.demo.service.ProjectRefService;
import com.example.demo.util.RoleAccessUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/project")
public class ProjectRefController {

    private final ProjectRefService projectService;
    private final CompanyRefService companyService;

    public ProjectRefController(ProjectRefService projectService, CompanyRefService companyService) {
        this.projectService = projectService;
        this.companyService = companyService;
    }

    // Show all projects
    @GetMapping("/admin/all")
    public String listProjects(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String edit,
            Model model,
            HttpSession session,
            @RequestHeader(value = "X-Requested-With", required = false) String ajaxHeader) {

        if (!RoleAccessUtil.canManageProjects(session)) {
            if ("XMLHttpRequest".equals(ajaxHeader)) {
                model.addAttribute("error", "You do not have permission to view projects.");
                return "projectList"; // renders projectList.html with error attribute
            }
            return "redirect:/Menu";
        }

        List<ProjectRef> projects;
        String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);

        if (search != null && !search.trim().isEmpty()) {
            projects = projectService.searchByCompanyNameOrCode(search.trim());
        } else {
            projects = projectService.getAll();
        }

        if (RoleAccessUtil.isBusinessManager(session) && companyCode != null) {
            projects = projects.stream()
                .filter(p -> p.getCompany() != null && p.getCompany().getCompanyCode().equals(companyCode))
                .collect(Collectors.toList());
        }

        model.addAttribute("projects", projects);
        model.addAttribute("project", new ProjectRef());
        model.addAttribute("searchTerm", search != null ? search : "");
        model.addAttribute("editProjectCode", edit != null ? edit : "");

        List<CompanyRef> companies;
        if (RoleAccessUtil.isBusinessManager(session) && companyCode != null) {
            companies = companyService.getAll().stream()
                .filter(c -> c.getCompanyCode().equals(companyCode))
                .collect(Collectors.toList());
        } else {
            companies = companyService.getAll();
        }
        model.addAttribute("companies", companies);

        return "projectList";
    }

    // Show form to add project
    @GetMapping("/add")
    public String showForm(Model model, HttpSession session) {
        // Only Admin and Business Manager can add projects
        if (!RoleAccessUtil.canManageProjects(session)) {
            model.addAttribute("error", "You do not have permission to add projects.");
            return "redirect:/Menu";
        }
        
        model.addAttribute("project", new ProjectRef());
        
        String role = (String) session.getAttribute("role");
        boolean canAddEdit = "ADMIN".equals(role) || "BUSINESS_MANAGER".equals(role);
        model.addAttribute("canAddEdit", canAddEdit);
        
        String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
        List<CompanyRef> companies;
        if (RoleAccessUtil.isBusinessManager(session) && companyCode != null) {
            companies = companyService.getAll().stream()
                .filter(c -> c.getCompanyCode().equals(companyCode))
                .collect(Collectors.toList());
        } else {
            companies = companyService.getAll();
        }
        model.addAttribute("companies", companies);
        return "projectList";
    }

    // Save or update project
    @PostMapping("/save")
    public String saveProject(@ModelAttribute ProjectRef project, 
                              @RequestParam(value = "companyCode", required = false) String companyCode,
                              HttpSession session,
                              Model model) {
        // Only Admin and Business Manager can save projects
        if (!RoleAccessUtil.canManageProjects(session)) {
            model.addAttribute("error", "You do not have permission to save projects.");
            return "redirect:/Menu";
        }
        
        String sessionCompanyCode = RoleAccessUtil.getCurrentCompanyCode(session);
        if (companyCode == null || companyCode.trim().isEmpty()) {
            companyCode = sessionCompanyCode;
        }
        if (RoleAccessUtil.isBusinessManager(session) && (companyCode == null || !companyCode.equals(sessionCompanyCode))) {
            companyCode = sessionCompanyCode;
        }
        
        if (companyCode == null || companyCode.trim().isEmpty()) {
            List<CompanyRef> allCompanies = companyService.getAll();
            if (!allCompanies.isEmpty()) {
                companyCode = allCompanies.get(0).getCompanyCode();
            } else {
                model.addAttribute("error", "No company assigned or available.");
                return "redirect:/project/admin/all";
            }
        }
        
        String finalCompanyCode = companyCode;
        CompanyRef company = companyService.getById(finalCompanyCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Company Code: " + finalCompanyCode));
        
        project.setCompany(company); // set company in project
        projectService.save(project); // save project
        return "redirect:/project/admin/all";
    }


    // Edit project
    @GetMapping("/edit/{projectCode}")
    public String editProject(@PathVariable String projectCode, Model model, HttpSession session) {
        // Only Admin and Business Manager can edit projects
        if (!RoleAccessUtil.canManageProjects(session)) {
            model.addAttribute("error", "You do not have permission to edit projects.");
            return "redirect:/Menu";
        }
        
        ProjectRef project = projectService.getById(projectCode).orElseThrow();
        
        // Business Manager can only edit projects of their company
        String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
        if (RoleAccessUtil.isBusinessManager(session) && 
            (project.getCompany() == null || !project.getCompany().getCompanyCode().equals(companyCode))) {
            model.addAttribute("error", "You can only edit projects of your company.");
            return "redirect:/project/admin/all";
        }
        
        // Redirect to project list with edit parameter to auto-open modal
        return "redirect:/project/admin/all?edit=" + projectCode;
    }

    // Delete project
    @GetMapping("/delete/{projectCode}")
    public String deleteProject(@PathVariable String projectCode, HttpSession session, Model model) {
        // Only Admin and Business Manager can delete projects
        if (!RoleAccessUtil.canManageProjects(session)) {
            model.addAttribute("error", "You do not have permission to delete projects.");
            return "redirect:/Menu";
        }
        
        ProjectRef project = projectService.getById(projectCode).orElseThrow();
        
        // Business Manager can only delete projects of their company
        String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
        if (RoleAccessUtil.isBusinessManager(session) && 
            (project.getCompany() == null || !project.getCompany().getCompanyCode().equals(companyCode))) {
            model.addAttribute("error", "You can only delete projects of your company.");
            return "redirect:/project/admin/all";
        }
        
        projectService.deleteById(projectCode);
        return "redirect:/project/admin/all";
    }
    
    // ============= API ENDPOINTS FOR ADMIN FILTERING =============
    
    // Get all projects as JSON (Admin only)
    @GetMapping("/api/all")
    @ResponseBody
    public List<Map<String, Object>> getAllProjectsJson(HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return List.of();
        }
        return projectService.getAll().stream()
            .map(this::projectToMap)
            .collect(Collectors.toList());
    }
    
    // Get projects filtered by company code (Admin only)
    @GetMapping("/api/by-company")
    @ResponseBody
    public List<Map<String, Object>> getProjectsByCompany(
            @RequestParam(required = false) String companyCode,
            HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return List.of();
        }
        
        List<ProjectRef> projects;
        if (companyCode == null || companyCode.isEmpty() || "ALL".equalsIgnoreCase(companyCode)) {
            projects = projectService.getAll();
        } else {
            projects = projectService.getByCompanyCode(companyCode);
        }
        
        return projects.stream()
            .map(this::projectToMap)
            .collect(Collectors.toList());
    }
    
    // Search projects by company name or code (Admin and Business Manager)
    @GetMapping("/api/search")
    @ResponseBody
    public List<Map<String, Object>> searchProjects(
            @RequestParam(required = false) String search,
            HttpSession session) {
        if (!RoleAccessUtil.canManageProjects(session)) {
            return List.of();
        }
        
        List<ProjectRef> projects;
        String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
        
        // If search term is provided, search by company name or code
        if (search != null && !search.trim().isEmpty()) {
            projects = projectService.searchByCompanyNameOrCode(search.trim());
        } else {
            projects = projectService.getAll();
        }
        
        // Business Manager can only see projects of their company
        if (RoleAccessUtil.isBusinessManager(session) && companyCode != null) {
            projects = projects.stream()
                .filter(p -> p.getCompany() != null && p.getCompany().getCompanyCode().equals(companyCode))
                .collect(Collectors.toList());
        }
        
        return projects.stream()
            .map(this::projectToMap)
            .collect(Collectors.toList());
    }
    
    // Get projects for Business Manager (filtered by their company)
    @GetMapping("/api/business-manager")
    @ResponseBody
    public List<Map<String, Object>> getBusinessManagerProjects(HttpSession session) {
        if (!RoleAccessUtil.isBusinessManager(session) && !RoleAccessUtil.isAdmin(session)) {
            return List.of();
        }
        
        String companyCode = RoleAccessUtil.getCurrentCompanyCode(session);
        List<ProjectRef> allProjects = projectService.getAll();
        List<ProjectRef> projects;
        if (companyCode != null && !companyCode.trim().isEmpty()) {
            String trimmed = companyCode.trim();
            projects = allProjects.stream()
                .filter(p -> p.getCompany() != null && p.getCompany().getCompanyCode() != null &&
                    (p.getCompany().getCompanyCode().equalsIgnoreCase(trimmed) ||
                     p.getCompany().getCompanyCode().toLowerCase().contains(trimmed.toLowerCase()) ||
                     trimmed.toLowerCase().contains(p.getCompany().getCompanyCode().toLowerCase())))
                .collect(Collectors.toList());
            if (projects.isEmpty()) {
                projects = allProjects;
            }
        } else {
            projects = allProjects;
        }
        
        return projects.stream()
            .map(this::projectToMap)
            .collect(Collectors.toList());
    }
    
    // Helper method to convert ProjectRef to Map for JSON
    private Map<String, Object> projectToMap(ProjectRef project) {
        Map<String, Object> map = new HashMap<>();
        map.put("projectCode", project.getProjectCode());
        map.put("projectName", project.getProjectName());
        map.put("execSponsor", project.getExecSponsor());
        map.put("busSponsor", project.getBusSponsor());
        map.put("budgetLead", project.getBudgetLead());
        map.put("businessGoal", project.getBusinessGoal());
        map.put("projectDescription", project.getProjectDescription());
        if (project.getCompany() != null) {
            map.put("companyCode", project.getCompany().getCompanyCode());
            map.put("companyName", project.getCompany().getCompanyName());
        }
        return map;
    }
}
