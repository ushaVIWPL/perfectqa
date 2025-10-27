package com.example.demo.controller;

import com.example.demo.entity.CompanyRef;
import com.example.demo.service.CompanyRefService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/company")
public class CompanyRefController {

    private final CompanyRefService service;

    public CompanyRefController(CompanyRefService service) {
        this.service = service;
    }

    // Show form to add new company
    @GetMapping("/add")
    public String showForm(Model model) {
        model.addAttribute("company", new CompanyRef());
        return "companyForm";
    }

    // Save or update company
    @PostMapping("/save")
    public String saveCompany(@ModelAttribute("company") CompanyRef company) {
        service.save(company);
        return "redirect:/users";
    }

    // Show all companies
    @GetMapping("/admin/all")
    public String listCompanies(Model model) {
        model.addAttribute("companies", service.getAll());
        model.addAttribute("company", new CompanyRef()); // Add this line
        model.addAttribute("c", new CompanyRef()); // for form binding

        return "companyList";
    }

    // Edit company
    @GetMapping("/edit/{code}")
    public String editCompany(@PathVariable String code, Model model) {
        CompanyRef company = service.getById(code).orElseThrow(() -> new IllegalArgumentException("Invalid ID"));
        model.addAttribute("company", company);
        return "companyForm";
    }

    // Delete company
    @GetMapping("/delete/{code}")
    public String deleteCompany(@PathVariable String code) {
        service.deleteById(code);
        return "redirect:/users";
    }
}
