package com.example.demo.controller;

import com.example.demo.entity.CompanyRef;
import com.example.demo.entity.ProjectRef;
import com.example.demo.service.CompanyRefService;
import com.example.demo.service.ProjectRefService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public String listProjects(Model model) {
        List<ProjectRef> projects = projectService.getAll();
        model.addAttribute("projects", projects);
        model.addAttribute("project", new ProjectRef());
        model.addAttribute("companies", companyService.getAll());
        return "projectList.html";
    }

    // Show form to add project
    @GetMapping("/add")
    public String showForm(Model model) {
        model.addAttribute("project", new ProjectRef());
        model.addAttribute("companies", companyService.getAll());
        return "projectForm";
    }

    // Save or update project
    @PostMapping("/save")
    public String saveProject(@ModelAttribute ProjectRef project, 
                              @RequestParam("companyCode") String companyCode) {
        // fetch the company based on code
        CompanyRef company = companyService.getById(companyCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Company Code"));
        
        project.setCompany(company); // set company in project
        projectService.save(project); // save project
        return "redirect:/users";
    }


    // Edit project
    @GetMapping("/edit/{projectCode}")
    public String editProject(@PathVariable String projectCode, Model model) {
        ProjectRef project = projectService.getById(projectCode).orElseThrow();
        model.addAttribute("project", project);
        model.addAttribute("companies", companyService.getAll());
        return "projectForm";
    }

    // Delete project
    @GetMapping("/delete/{projectCode}")
    public String deleteProject(@PathVariable String projectCode) {
        projectService.deleteById(projectCode);
        return "redirect:/project/all";
    }
}
