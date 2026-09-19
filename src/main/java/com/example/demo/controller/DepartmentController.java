package com.example.demo.controller;

import com.example.demo.entity.CompanyRef;
import com.example.demo.entity.Department;
import com.example.demo.service.CompanyRefService;
import com.example.demo.service.DepartmentService;
import com.example.demo.util.RoleAccessUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/department")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final CompanyRefService companyService;

    public DepartmentController(DepartmentService departmentService, CompanyRefService companyService) {
        this.departmentService = departmentService;
        this.companyService = companyService;
    }

    @GetMapping("/list")
    public String listDepartments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String companyFilter,
            @RequestParam(required = false) String edit,
            Model model,
            HttpSession session,
            @RequestHeader(value = "X-Requested-With", required = false) String ajaxHeader) {

        String role = RoleAccessUtil.getCurrentRole(session);
        boolean isBusinessManager = RoleAccessUtil.isBusinessManager(session);
        boolean isAdmin = RoleAccessUtil.isAdmin(session);

        if (!isBusinessManager && !isAdmin) {
            if ("XMLHttpRequest".equals(ajaxHeader)) {
                model.addAttribute("error", "You do not have permission to view departments.");
                return "departmentList";
            }
            return "redirect:/Menu";
        }

        if (!"XMLHttpRequest".equalsIgnoreCase(ajaxHeader)) {
            if (isBusinessManager) {
                return "redirect:/business-manager?module=departments";
            }
            if (isAdmin) {
                return "redirect:/admin?module=departments";
            }
        }

        List<Department> departments;
        String sessionCompanyCode = RoleAccessUtil.getCurrentCompanyCode(session);

        if (isBusinessManager) {
            if (search != null && !search.trim().isEmpty()) {
                departments = departmentService.search(search, sessionCompanyCode);
            } else {
                departments = departmentService.getByCompanyCode(sessionCompanyCode);
            }
        } else {
            // Admin flow: show by companyFilter or all
            if (companyFilter != null && !companyFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(companyFilter)) {
                if (search != null && !search.trim().isEmpty()) {
                    departments = departmentService.search(search, companyFilter);
                } else {
                    departments = departmentService.getByCompanyCode(companyFilter);
                }
            } else if (search != null && !search.trim().isEmpty()) {
                departments = departmentService.search(search, null);
            } else {
                departments = departmentService.getAll();
            }
        }

        model.addAttribute("departments", departments);
        model.addAttribute("department", new Department());
        model.addAttribute("searchTerm", search != null ? search : "");
        model.addAttribute("companyFilter", companyFilter != null ? companyFilter : "ALL");
        model.addAttribute("editDepartmentCode", edit != null ? edit : "");

        // Only Business Manager can add/edit/delete
        model.addAttribute("canAddEdit", isBusinessManager);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isBusinessManager", isBusinessManager);

        List<CompanyRef> companies = companyService.getAll();
        model.addAttribute("companies", companies);
        model.addAttribute("currentCompanyCode", sessionCompanyCode);

        return "departmentList";
    }

    @PostMapping("/save")
    public Object saveDepartment(@ModelAttribute Department department,
                                 @RequestParam(value = "isEdit", required = false) String isEdit,
                                 @RequestParam(value = "originalDepartmentCode", required = false) String originalDepartmentCode,
                                 HttpSession session,
                                 Model model,
                                 @RequestHeader(value = "X-Requested-With", required = false) String ajaxHeader) {

        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(ajaxHeader);

        if (!RoleAccessUtil.isBusinessManager(session)) {
            String msg = "Only Business Managers can create or update departments.";
            if (isAjax) return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", msg));
            model.addAttribute("error", msg);
            return "redirect:/department/list";
        }

        String sessionCompanyCode = RoleAccessUtil.getCurrentCompanyCode(session);
        if (sessionCompanyCode == null || sessionCompanyCode.trim().isEmpty()) {
            String msg = "No company associated with your Business Manager account.";
            if (isAjax) return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", msg));
            model.addAttribute("error", msg);
            return "redirect:/department/list";
        }

        CompanyRef company = companyService.getById(sessionCompanyCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Company Code for session"));

        if (department.getDepartmentCode() == null || department.getDepartmentCode().trim().isEmpty()) {
            String msg = "Department Code is required.";
            if (isAjax) return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", msg));
            model.addAttribute("error", msg);
            return "redirect:/department/list";
        }

        // Check for duplicate code on creation or when changing code
        boolean editing = "true".equalsIgnoreCase(isEdit);
        if (!editing || (originalDepartmentCode != null && !originalDepartmentCode.equals(department.getDepartmentCode()))) {
            Optional<Department> existing = departmentService.getById(department.getDepartmentCode().trim());
            if (existing.isPresent()) {
                String msg = "A department with code '" + department.getDepartmentCode() + "' already exists.";
                if (isAjax) return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", msg));
                model.addAttribute("error", msg);
                return "redirect:/department/list";
            }
        }

        department.setDepartmentCode(department.getDepartmentCode().trim());
        if (department.getStatus() == null || department.getStatus().trim().isEmpty()) {
            department.setStatus("Active");
        }
        department.setCompany(company);

        departmentService.save(department);
        if (isAjax) {
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("success", true, "message", "Department saved successfully."));
        }
        return "redirect:/department/list";
    }

    @GetMapping("/edit/{departmentCode}")
    public String editDepartment(@PathVariable String departmentCode, Model model, HttpSession session) {
        if (!RoleAccessUtil.isBusinessManager(session)) {
            model.addAttribute("error", "Only Business Managers can edit departments.");
            return "redirect:/department/list";
        }

        Department department = departmentService.getById(departmentCode).orElseThrow();
        String sessionCompanyCode = RoleAccessUtil.getCurrentCompanyCode(session);

        if (department.getCompany() == null || !department.getCompany().getCompanyCode().equals(sessionCompanyCode)) {
            model.addAttribute("error", "You can only edit departments of your own company.");
            return "redirect:/department/list";
        }

        return "redirect:/department/list?edit=" + departmentCode;
    }

    @GetMapping("/delete/{departmentCode}")
    public Object deleteDepartment(@PathVariable String departmentCode, HttpSession session, Model model,
                                   @RequestHeader(value = "X-Requested-With", required = false) String ajaxHeader) {
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(ajaxHeader);

        if (!RoleAccessUtil.isBusinessManager(session)) {
            String msg = "Only Business Managers can delete departments.";
            if (isAjax) return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", msg));
            model.addAttribute("error", msg);
            return "redirect:/department/list";
        }

        Optional<Department> opt = departmentService.getById(departmentCode);
        if (opt.isPresent()) {
            Department department = opt.get();
            String sessionCompanyCode = RoleAccessUtil.getCurrentCompanyCode(session);
            if (department.getCompany() == null || !department.getCompany().getCompanyCode().equals(sessionCompanyCode)) {
                String msg = "You can only delete departments of your own company.";
                if (isAjax) return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", msg));
                model.addAttribute("error", msg);
                return "redirect:/department/list";
            }
            departmentService.deleteById(departmentCode);
        }

        if (isAjax) {
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("success", true, "message", "Department deleted successfully."));
        }
        return "redirect:/department/list";
    }

    @GetMapping("/api/by-company")
    @ResponseBody
    public List<Map<String, Object>> getDepartmentsByCompany(@RequestParam(required = false) String companyCode,
                                                             HttpSession session) {
        String code = companyCode;
        if (RoleAccessUtil.isBusinessManager(session)) {
            String sessionCode = RoleAccessUtil.getCurrentCompanyCode(session);
            if (sessionCode != null && !sessionCode.trim().isEmpty()) {
                code = sessionCode;
            }
        }
        
        List<Department> list;
        if (code != null && !code.trim().isEmpty()) {
            list = departmentService.getByCompanyCode(code.trim());
        } else {
            list = departmentService.getAll();
        }
        if (list == null || list.isEmpty()) {
            list = departmentService.getAll();
        }

        return list.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("departmentCode", d.getDepartmentCode());
            map.put("departmentName", d.getDepartmentName());
            map.put("description", d.getDescription() != null ? d.getDescription() : "");
            map.put("headOfDepartment", d.getHeadOfDepartment() != null ? d.getHeadOfDepartment() : "");
            map.put("status", d.getStatus() != null ? d.getStatus() : "Active");
            return map;
        }).collect(Collectors.toList());
    }
}
