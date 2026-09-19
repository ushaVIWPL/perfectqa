package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.BusinessScenario;
import com.example.demo.entity.BusinessScenarioId;
import com.example.demo.entity.CompanyRef;
import com.example.demo.service.BusinessScenarioService;
import com.example.demo.service.CompanyRefService;
import com.example.demo.util.RoleAccessUtil;

import jakarta.servlet.http.HttpSession;

@Controller
public class BusinessScenarioController {

    @Autowired
    private BusinessScenarioService scenarioService;
    
    @Autowired
    private CompanyRefService companyService;

    // ----------------- Web Views -----------------

    @GetMapping("/save")
    public String saveScenarioGet() {
        return "redirect:/addheaderscenario"; // redirect to form
    }

    
    // List all scenarios with pagination
    @GetMapping("/list")
    public String listScenarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id.businessScenario") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            Model model, 
            HttpSession session) {
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null) {
            model.addAttribute("error", "Company not found in session. Please login again.");
            return "redirect:/login";
        }
        
        // Use pagination for better performance
        Page<BusinessScenario> scenariosPage;
        if (search != null && !search.trim().isEmpty()) {
            scenariosPage = scenarioService.searchScenariosByCompany(companyCode, search, page, size, sortBy, sortDir);
        } else {
            scenariosPage = scenarioService.getScenariosByCompany(companyCode, page, size, sortBy, sortDir);
        }
        
        model.addAttribute("scenarios", scenariosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", scenariosPage.getTotalPages());
        model.addAttribute("totalItems", scenariosPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        
        // Add accurate counts for metrics
        model.addAttribute("responsibleCount", scenarioService.getResponsibleCount(companyCode));
        model.addAttribute("workStreamCount", scenarioService.getWorkStreamCount(companyCode));
        
        // Add role information for template
        String role = RoleAccessUtil.getCurrentRole(session);
        model.addAttribute("userRole", role);
        model.addAttribute("isTester", RoleAccessUtil.isTester(session));
        model.addAttribute("canAddEdit", RoleAccessUtil.canAddEdit(session));
        
        Map<String, Boolean> hasChildrenMap = new java.util.HashMap<>();
        for (BusinessScenario s : scenariosPage.getContent()) {
            hasChildrenMap.put(s.getId().getBusinessScenario(), scenarioService.hasChildren(companyCode, s.getId().getBusinessScenario()));
        }
        model.addAttribute("hasChildrenMap", hasChildrenMap);
        
        return "headerscenariolist";
    }
    
    @GetMapping("/list/{page}/{companyCode}")
    public String listScenarios(
            @PathVariable int page,
            @PathVariable String companyCode,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id.businessScenario") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            Model model,
            HttpSession session) {

        // save companyCode into session (optional)
        session.setAttribute("companyCode", companyCode);

        Page<BusinessScenario> scenariosPage;

        if (search != null && !search.trim().isEmpty()) {
            scenariosPage = scenarioService.searchScenariosByCompany(
                    companyCode, search, page, size, sortBy, sortDir);
        } else {
            scenariosPage = scenarioService.getScenariosByCompany(
                    companyCode, page, size, sortBy, sortDir);
        }

        model.addAttribute("scenarios", scenariosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", scenariosPage.getTotalPages());
        model.addAttribute("totalItems", scenariosPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);

        // Add accurate counts for metrics
        model.addAttribute("responsibleCount", scenarioService.getResponsibleCount(companyCode));
        model.addAttribute("workStreamCount", scenarioService.getWorkStreamCount(companyCode));

        model.addAttribute("userRole", RoleAccessUtil.getCurrentRole(session));
        model.addAttribute("isTester", RoleAccessUtil.isTester(session));
        model.addAttribute("canAddEdit", RoleAccessUtil.canAddEdit(session));

        Map<String, Boolean> hasChildrenMap = new java.util.HashMap<>();
        for (BusinessScenario s : scenariosPage.getContent()) {
            hasChildrenMap.put(s.getId().getBusinessScenario(), scenarioService.hasChildren(companyCode, s.getId().getBusinessScenario()));
        }
        model.addAttribute("hasChildrenMap", hasChildrenMap);

        return "headerscenariolist";
    }



    // Show add scenario form
    @GetMapping("/addbusinessscenario")
    public String showAddBusinessScenarioForm(Model model, HttpSession session) {
        // Restrict tester access - read only
        if (RoleAccessUtil.isTester(session)) {
            model.addAttribute("error", "Testers do not have permission to add business scenarios.");
            return "redirect:/list";
        }
        
        // Check if user is logged in
        if (session.getAttribute("userId") == null) {
            return "redirect:/loginform";
        }

        String companyCode = (String) session.getAttribute("companyCode");
        
        // If no company in session, get the first available company
        List<CompanyRef> companies = companyService.getAll();
        if ((companyCode == null || companyCode.isEmpty()) && !companies.isEmpty()) {
            companyCode = companies.get(0).getCompanyCode();
            // Also update session for future use
            session.setAttribute("companyCode", companyCode);
        }
        
        BusinessScenario scenario = new BusinessScenario();
        BusinessScenarioId id = new BusinessScenarioId();
        
        // Set company code
        if (companyCode != null) {
            id.setCompanyCode(companyCode);
            companyService.getById(companyCode).ifPresent(scenario::setCompany);
        }
        
        scenario.setId(id);
        model.addAttribute("scenario", scenario);
        model.addAttribute("companies", companies);
        model.addAttribute("editMode", false);

        return "addheaderscenario";
    }




    // View scenario details (read-only, available for all roles)
    @GetMapping("/viewbusinessscenario/{businessScenario}")
    public String viewBusinessScenario(@PathVariable String businessScenario,
                                       HttpSession session,
                                       Model model) {
        // Check if user is logged in
        if (session.getAttribute("userId") == null) {
            return "redirect:/loginform";
        }

        // Get company code from session
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "Company code not found in session. Please login again.");
            return "redirect:/list";
        }

        // Create the composite ID
        BusinessScenarioId id = new BusinessScenarioId();
        id.setCompanyCode(companyCode);
        id.setBusinessScenario(businessScenario);

        // Find the existing scenario
        Optional<BusinessScenario> scenarioOpt = scenarioService.getScenarioById(id);
        if (scenarioOpt.isEmpty()) {
            model.addAttribute("error", "Business Scenario '" + businessScenario + "' not found for your company.");
            return "redirect:/list";
        }

        BusinessScenario scenario = scenarioOpt.get();
        model.addAttribute("scenario", scenario);
        
        // Add role information for template
        String role = RoleAccessUtil.getCurrentRole(session);
        model.addAttribute("userRole", role);
        model.addAttribute("canAddEdit", RoleAccessUtil.canAddEdit(session));

        return "view-business-scenario";
    }

    // Show edit scenario form (by Business Scenario identifier - gets company code from session)
    @GetMapping("/editbusinessscenario/{businessScenario}")
    public String showEditBusinessScenarioByScenario(@PathVariable String businessScenario,
                                                      HttpSession session,
                                                      Model model) {
        // Restrict tester access - read only
        if (RoleAccessUtil.isTester(session)) {
            model.addAttribute("error", "Testers do not have permission to edit business scenarios.");
            return "redirect:/list";
        }
        
        // Check if user is logged in
        if (session.getAttribute("userId") == null) {
            return "redirect:/loginform";
        }

        // Get company code from session
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "Company code not found in session. Please login again.");
            return "redirect:/list";
        }

        // Create the composite ID
        BusinessScenarioId id = new BusinessScenarioId();
        id.setCompanyCode(companyCode);
        id.setBusinessScenario(businessScenario);

        // Find the existing scenario
        Optional<BusinessScenario> scenarioOpt = scenarioService.getScenarioById(id);
        if (scenarioOpt.isEmpty()) {
            model.addAttribute("error", "Business Scenario '" + businessScenario + "' not found for your company.");
            return "redirect:/list";
        }

        BusinessScenario scenario = scenarioOpt.get();
        model.addAttribute("scenario", scenario);
        model.addAttribute("companies", companyService.getAll());
        model.addAttribute("editMode", true);

        return "addheaderscenario";
    }
    
    // Show edit scenario form (legacy - with both companyCode and businessScenario parameters)
    @GetMapping("/editbusinessscenario")
    public String showEditBusinessScenarioForm(@RequestParam String companyCode,
                                               @RequestParam String businessScenario,
                                               HttpSession session,
                                               Model model) {
        // Restrict tester access - read only
        if (RoleAccessUtil.isTester(session)) {
            model.addAttribute("error", "Testers do not have permission to edit business scenarios.");
            return "redirect:/list";
        }
        
        // Check if user is logged in
        if (session.getAttribute("userId") == null) {
            return "redirect:/loginform";
        }

        // Verify company code from session
        String sessionCompanyCode = (String) session.getAttribute("companyCode");
        if (sessionCompanyCode == null || !sessionCompanyCode.equals(companyCode)) {
            model.addAttribute("error", "Company code mismatch. Please login again.");
            return "redirect:/list";
        }

        // Create the composite ID
        BusinessScenarioId id = new BusinessScenarioId();
        id.setCompanyCode(companyCode);
        id.setBusinessScenario(businessScenario);

        // Find the existing scenario
        Optional<BusinessScenario> scenarioOpt = scenarioService.getScenarioById(id);
        if (scenarioOpt.isEmpty()) {
            model.addAttribute("error", "Business Scenario not found.");
            return "redirect:/list";
        }

        BusinessScenario scenario = scenarioOpt.get();
        model.addAttribute("scenario", scenario);
        model.addAttribute("companies", companyService.getAll());
        model.addAttribute("editMode", true);

        return "addheaderscenario";
    }

    // Save scenario (for both add & edit)
    @PostMapping("/save")
    public String saveScenario(@ModelAttribute BusinessScenario scenario,
                               @RequestParam(required = false, defaultValue = "false") boolean editMode,
                               HttpSession session,
                               Model model) {
        // Restrict tester access - read only
        if (RoleAccessUtil.isTester(session)) {
            model.addAttribute("error", "Testers do not have permission to save business scenarios.");
            return "redirect:/list";
        }

        System.out.println("=== SAVE SCENARIO CALLED ===");
        System.out.println("Scenario ID: " + scenario.getId());
        System.out.println("Session companyCode: " + session.getAttribute("companyCode"));

        // Validate Business Scenario field
        if (scenario.getId() == null || scenario.getId().getBusinessScenario() == null
                || scenario.getId().getBusinessScenario().trim().isEmpty()) {
            model.addAttribute("error", "Business Scenario is required and must be 1-3 digits only.");
            model.addAttribute("scenario", scenario);
            model.addAttribute("companies", companyService.getAll());
            model.addAttribute("editMode", false);
            return "addheaderscenario";
        }

        // Validate Business Scenario is numeric and max 3 digits
        String businessScenario = scenario.getId().getBusinessScenario().trim();
        if (!businessScenario.matches("^[0-9]{1,3}$")) {
            model.addAttribute("error", "Business Scenario must be 1-3 digits only (numbers only).");
            model.addAttribute("scenario", scenario);
            model.addAttribute("companies", companyService.getAll());
            model.addAttribute("editMode", false);
            return "addheaderscenario";
        }

        // ALWAYS get company code from session first
        String companyCode = (String) session.getAttribute("companyCode");

        System.out.println("Company code from session: " + companyCode);

        // If no company in session, try to get first available company
        if (companyCode == null || companyCode.isEmpty()) {
            List<CompanyRef> companies = companyService.getAll();
            if (!companies.isEmpty()) {
                companyCode = companies.get(0).getCompanyCode();
                session.setAttribute("companyCode", companyCode);
                System.out.println("Using first available company: " + companyCode);
            }
        }

        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "No company available. Please contact admin to add a company first.");
            model.addAttribute("scenario", scenario);
            model.addAttribute("companies", companyService.getAll());
            model.addAttribute("editMode", false);
            return "addheaderscenario";
        }

        Optional<CompanyRef> companyOpt = companyService.getById(companyCode);
        System.out.println("Company found: " + companyOpt.isPresent());

        if (companyOpt.isEmpty()) {
            model.addAttribute("error", "Invalid company code: " + companyCode);
            model.addAttribute("scenario", scenario);
            model.addAttribute("companies", companyService.getAll());
            model.addAttribute("editMode", false);
            return "addheaderscenario";
        }

        // Set company on entity
        scenario.setCompany(companyOpt.get());

        // Ensure ID exists
        if (scenario.getId() == null) {
            scenario.setId(new BusinessScenarioId());
        }

        // ✅ Set companyCode on the ID BEFORE any existence/duplicate check
        scenario.getId().setCompanyCode(companyCode);

        System.out.println("Business Scenario Name: " + scenario.getId().getBusinessScenario());
        System.out.println("Company Code set to: " + scenario.getId().getCompanyCode());

        // ✅ Now the composite key (companyCode + businessScenario) is fully formed
        boolean exists = scenarioService.existsById(scenario.getId());
        System.out.println("Already exists: " + exists);

        // If adding new scenario, check for duplicate using existsById directly
        if (!editMode && exists) {
            model.addAttribute("error", "Duplicate Scenario Number! This scenario already exists for this company.");
            model.addAttribute("scenario", scenario);
            model.addAttribute("companies", companyService.getAll());
            model.addAttribute("editMode", false);
            return "addheaderscenario";
        }

        // Save or update scenario
        try {
            scenarioService.saveScenario(scenario);
            System.out.println("=== SCENARIO SAVED SUCCESSFULLY ===");
        } catch (Exception e) {
            System.out.println("=== ERROR SAVING SCENARIO ===");
            e.printStackTrace();
            model.addAttribute("error", "Error saving scenario: " + e.getMessage());
            model.addAttribute("scenario", scenario);
            model.addAttribute("companies", companyService.getAll());
            model.addAttribute("editMode", false);
            return "addheaderscenario";
        }

        return "redirect:/list";
    }

    @GetMapping("/api/scenarios/exists")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkScenarioExists(
            @RequestParam String businessScenario,
            HttpSession session) {
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty() || businessScenario == null || businessScenario.trim().isEmpty()) {
            return ResponseEntity.ok(Map.of("exists", false));
        }
        BusinessScenarioId id = new BusinessScenarioId();
        id.setCompanyCode(companyCode);
        id.setBusinessScenario(businessScenario.trim());
        boolean exists = scenarioService.existsById(id);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping("/ajax/delete/{businessScenario}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteScenario(@PathVariable String businessScenario, HttpSession session) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String role = RoleAccessUtil.getCurrentRole(session);
            if (!"ADMIN".equals(role) && !"BUSINESS_MANAGER".equals(role)) {
                response.put("success", false);
                response.put("message", "You don't have permission to delete.");
                return ResponseEntity.ok(response);
            }
            
            String companyCode = (String) session.getAttribute("companyCode");
            if (companyCode == null || companyCode.isEmpty()) {
                response.put("success", false);
                response.put("message", "Session expired or company code missing.");
                return ResponseEntity.ok(response);
            }

            if (scenarioService.hasChildren(companyCode, businessScenario)) {
                response.put("success", false);
                response.put("message", "Cannot delete because there are dependent activities.");
                return ResponseEntity.ok(response);
            }

            scenarioService.deleteScenario(companyCode, businessScenario);
            response.put("success", true);
            response.put("message", "Business Scenario deleted successfully.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error deleting scenario: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
