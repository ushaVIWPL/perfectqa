package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.embeddedid.ScenarioActivitiesId;
import com.example.demo.entity.BusinessScenario;
import com.example.demo.entity.BusinessScenarioId;
import com.example.demo.entity.ScenarioActivities;
import com.example.demo.service.BusinessScenarioService;
import com.example.demo.service.ScenarioActivitiesService;
import com.example.demo.util.RoleAccessUtil;

import jakarta.servlet.http.HttpSession;

@Controller
public class ScenarioActivitiesController {

    @Autowired
    private ScenarioActivitiesService activitiesService;

    @Autowired
    private BusinessScenarioService scenarioService;

    // Show form to add activity for a specific business scenario
    @GetMapping("/addactivity")
    public String showAddActivityForm(@RequestParam(required = false) String companyCode,
                                      @RequestParam String businessScenario,
                                      HttpSession session,
                                      Model model) {
        // Restrict tester access - read only
        if (RoleAccessUtil.isTester(session)) {
            model.addAttribute("error", "Testers do not have permission to add activities.");
            return "redirect:/list";
        }

        // Get company code from session if not provided in request
        if (companyCode == null || companyCode.isEmpty()) {
            companyCode = (String) session.getAttribute("companyCode");
        }

        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "Company code not found. Please login again.");
            return "redirect:/loginform";
        }

        BusinessScenarioId scenarioId = new BusinessScenarioId();
        scenarioId.setBusinessScenario(businessScenario);
        scenarioId.setCompanyCode(companyCode);

        BusinessScenario scenario = scenarioService.getScenarioById(scenarioId)
            .orElseThrow(() -> new IllegalArgumentException("Business Scenario not found"));

        ScenarioActivities activity = new ScenarioActivities();
        activity.setBusinessScenario(scenario);

        ScenarioActivitiesId id = new ScenarioActivitiesId();
        BusinessScenarioId businessScenarioId = new BusinessScenarioId();
        businessScenarioId.setBusinessScenario(businessScenario);
        businessScenarioId.setCompanyCode(companyCode);
        id.setBusinessScenarioId(businessScenarioId);

        activity.setId(id);

        model.addAttribute("activity", activity);
        model.addAttribute("editMode", false);
        return "addactivity";
    }

    // View activity details (read-only, available for all roles)
    @GetMapping("/viewactivity/{transactionKey}")
    public String viewActivity(@PathVariable String transactionKey,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("userId") == null) {
            return "redirect:/loginform";
        }

        // Get company code from session
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Company code not found in session. Please login again.");
            return "redirect:/activities";
        }

        // Find the activity by transaction key
        Optional<ScenarioActivities> activityOpt = activitiesService.getActivityByTransactionKey(transactionKey);
        if (activityOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Activity with Transaction Key '" + transactionKey + "' not found.");
            return "redirect:/activities";
        }

        ScenarioActivities activity = activityOpt.get();
        String description =
        	    activity.getBusinessScenario().getScenarioDescription();

        	model.addAttribute("scenarioDescription", description);
        // Verify the activity belongs to the user's company
        if (activity.getId() != null && 
            activity.getId().getBusinessScenarioId() != null &&
            !companyCode.equals(activity.getId().getBusinessScenarioId().getCompanyCode())) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to view this activity.");
            return "redirect:/activities";
        }

        model.addAttribute("activity", activity);
        
        // Add role information for template
        String role = RoleAccessUtil.getCurrentRole(session);
        model.addAttribute("userRole", role);
        model.addAttribute("canAddEdit", RoleAccessUtil.canAddEdit(session));

        return "view-scenario-activity";
    }

    // Show form to edit activity by Transaction Key (simplified - gets company code from session)
    @GetMapping("/editactivity/{transactionKey}")
    public String showEditActivityByTransactionKey(@PathVariable String transactionKey,
                                                   HttpSession session,
                                                   Model model) {
        // Restrict tester access - read only
        if (RoleAccessUtil.isTester(session)) {
            model.addAttribute("error", "Testers do not have permission to edit activities.");
            return "redirect:/activities";
        }

        // Get company code from session
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "Company code not found in session. Please login again.");
            return "redirect:/activities";
        }

        // Find the activity by transaction key
        Optional<ScenarioActivities> activityOpt = activitiesService.getActivityByTransactionKey(transactionKey);
        if (activityOpt.isEmpty()) {
            model.addAttribute("error", "Activity with Transaction Key '" + transactionKey + "' not found.");
            return "redirect:/activities";
        }

        ScenarioActivities activity = activityOpt.get();
        
        // Verify the activity belongs to the user's company
        if (activity.getId() != null && 
            activity.getId().getBusinessScenarioId() != null &&
            !companyCode.equals(activity.getId().getBusinessScenarioId().getCompanyCode())) {
            model.addAttribute("error", "You do not have permission to edit this activity.");
            return "redirect:/activities";
        }

        model.addAttribute("activity", activity);
        model.addAttribute("editMode", true);
        return "addactivity";
    }
    
    // Show form to edit activity (legacy - with companyCode, businessScenario, and transactionSuffix parameters)
    @GetMapping("/editactivity")
    public String showEditActivityForm(@RequestParam String companyCode,
                                      @RequestParam String businessScenario,
                                      @RequestParam String transactionSuffix,
                                      HttpSession session,
                                      Model model) {
        // Restrict tester access - read only
        if (RoleAccessUtil.isTester(session)) {
            model.addAttribute("error", "Testers do not have permission to edit activities.");
            return "redirect:/list";
        }

        // Get company code from session if not provided
        if (companyCode == null || companyCode.isEmpty()) {
            companyCode = (String) session.getAttribute("companyCode");
        }

        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "Company code not found. Please login again.");
            return "redirect:/loginform";
        }

        // Create the composite ID
        ScenarioActivitiesId id = new ScenarioActivitiesId();
        BusinessScenarioId businessScenarioId = new BusinessScenarioId();
        businessScenarioId.setBusinessScenario(businessScenario);
        businessScenarioId.setCompanyCode(companyCode);
        id.setBusinessScenarioId(businessScenarioId);
        id.setTransactionSuffix(transactionSuffix);

        // Find the existing activity
        Optional<ScenarioActivities> activityOpt = activitiesService.getActivityById(id);
        if (activityOpt.isEmpty()) {
            model.addAttribute("error", "Activity not found.");
            return "redirect:/activities";
        }

        ScenarioActivities activity = activityOpt.get();
        model.addAttribute("activity", activity);
        model.addAttribute("editMode", true);
        return "addactivity";
    }

    @PostMapping("/saveactivity")
    public String saveActivity(@ModelAttribute ScenarioActivities activity, 
                               HttpSession session,
                               Model model) {
        // Restrict tester access - read only
        if (RoleAccessUtil.isTester(session)) {
            model.addAttribute("error", "Testers do not have permission to save activities.");
            return "redirect:/list";
        }

        System.out.println("=== SAVE ACTIVITY CALLED ===");
        System.out.println("Activity ID: " + activity.getId());

        if (activity.getId() == null) {
            activity.setId(new ScenarioActivitiesId());
        }

        ScenarioActivitiesId id = activity.getId();
        
        // Initialize businessScenarioId if null
        if (id.getBusinessScenarioId() == null) {
            id.setBusinessScenarioId(new BusinessScenarioId());
        }

        // Get company code from session if not in form
        String companyCode = null;
        if (id.getBusinessScenarioId() != null) {
            companyCode = id.getBusinessScenarioId().getCompanyCode();
        }
        
        if (companyCode == null || companyCode.trim().isEmpty()) {
            companyCode = (String) session.getAttribute("companyCode");
            if (companyCode != null && id.getBusinessScenarioId() != null) {
                id.getBusinessScenarioId().setCompanyCode(companyCode);
            }
        }

        // Validate Transaction Suffix - must be numbers only
        String businessScenario = id.getBusinessScenarioId() != null ? id.getBusinessScenarioId().getBusinessScenario() : null;

        // Auto-generate transactionSuffix if not provided
        String transactionSuffix = id.getTransactionSuffix();
        if (transactionSuffix == null || transactionSuffix.trim().isEmpty()) {
            if (businessScenario != null && !businessScenario.trim().isEmpty() && companyCode != null && !companyCode.trim().isEmpty()) {
                transactionSuffix = activitiesService.generateNextTransactionSuffix(companyCode, businessScenario);
                id.setTransactionSuffix(transactionSuffix);
                System.out.println("Auto-generated transactionSuffix: " + transactionSuffix);
            }
        } else {
            // Validate Transaction Suffix - must be numbers only
            if (!transactionSuffix.matches("^[0-9]+$")) {
                model.addAttribute("error", "Transaction Suffix must contain only numbers.");
                model.addAttribute("activity", activity);
                model.addAttribute("editMode", activitiesService.activityExists(id));
                return "addactivity";
            }
        }
        
        System.out.println("Transaction Suffix: " + id.getTransactionSuffix());
        System.out.println("Business Scenario: " + businessScenario);
        System.out.println("Company Code: " + companyCode);

        // Validate required fields
        if (id.getBusinessScenarioId() == null ||
            id.getBusinessScenarioId().getBusinessScenario() == null || 
            id.getBusinessScenarioId().getBusinessScenario().trim().isEmpty() ||
            companyCode == null || 
            companyCode.trim().isEmpty() ||
            id.getTransactionSuffix() == null ||
            id.getTransactionSuffix().trim().isEmpty()) {

            model.addAttribute("error", "Business Scenario, Company Code, and Transaction Suffix are required.");
            model.addAttribute("activity", activity);
            return "addactivity";
        }
        
        // Set company code in businessScenarioId
        id.getBusinessScenarioId().setCompanyCode(companyCode);

        // Check if this is an edit (activity already exists) or new (add)
        boolean isEdit = activitiesService.activityExists(id);
        
        // If adding new activity, check for duplicate transaction suffix
        if (!isEdit) {
            // Check if transaction suffix already exists for this business scenario and company
            List<ScenarioActivities> existingActivities = activitiesService.getActivitiesByCompanyCode(companyCode);
            boolean duplicateSuffix = existingActivities.stream()
                .anyMatch(a -> a.getId().getBusinessScenarioId().getBusinessScenario().equals(id.getBusinessScenarioId().getBusinessScenario())
                           && a.getId().getTransactionSuffix().equals(id.getTransactionSuffix()));
            
            if (duplicateSuffix) {
                model.addAttribute("error", "Transaction Suffix '" + id.getTransactionSuffix() + "' already exists for this Business Scenario.");
                model.addAttribute("activity", activity);
                model.addAttribute("editMode", false);
                return "addactivity";
            }
        }

        // Fetch scenario
        BusinessScenarioId scenarioId = new BusinessScenarioId();
        scenarioId.setBusinessScenario(id.getBusinessScenarioId().getBusinessScenario());
        scenarioId.setCompanyCode(id.getBusinessScenarioId().getCompanyCode());
        
        BusinessScenario scenario = scenarioService.getScenarioById(scenarioId)
            .orElseThrow(() -> new IllegalArgumentException("Business Scenario not found"));

        activity.setBusinessScenario(scenario);

        if (isEdit) {
            activitiesService.getActivityById(id).ifPresent(existing -> {
                activity.setTransactionKey(existing.getTransactionKey());
            });
        }

        try {
            // Save or update activity
            activitiesService.saveActivity(activity);
        } catch (DataIntegrityViolationException e) {
            // Handle database constraint violations (e.g., data too long)
            String errorMessage = "Failed to save activity. ";
            String exceptionMessage = e.getMessage();
            
            if (exceptionMessage != null) {
                if (exceptionMessage.contains("Data too long") || exceptionMessage.contains("value too long")) {
                    errorMessage += "One or more fields contain text that is too long. Please reduce the text length.";
                } else if (exceptionMessage.contains("Duplicate entry")) {
                    errorMessage += "A duplicate entry already exists.";
                } else {
                    errorMessage += "Database error: " + exceptionMessage;
                }
            } else {
                errorMessage += "The data you entered exceeds the maximum allowed length for one or more fields.";
            }
            
            model.addAttribute("error", errorMessage);
            model.addAttribute("activity", activity);
            model.addAttribute("editMode", isEdit);
            return "addactivity";
        } catch (Exception e) {
            // Handle any other exceptions
            String errorMessage = "An error occurred while saving the activity: ";
            if (e.getMessage() != null) {
                errorMessage += e.getMessage();
            } else {
                errorMessage += "Unknown error. Please try again.";
            }
            
            model.addAttribute("error", errorMessage);
            model.addAttribute("activity", activity);
            model.addAttribute("editMode", isEdit);
            return "addactivity";
        }

        return "redirect:/activities";
    }
    
    @GetMapping("/activities")
    public String listActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id.transactionSuffix") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            HttpSession session, 
            Model model) {

        String companyCode = (String) session.getAttribute("companyCode");

        if (companyCode == null) {
            throw new IllegalArgumentException("Company Code not found in session");
        }

        // Use pagination for better performance
        Page<ScenarioActivities> activitiesPage;
        if (search != null && !search.trim().isEmpty()) {
            activitiesPage = activitiesService.searchActivitiesByCompanyCode(companyCode, search, page, size, sortBy, sortDir);
        } else {
            activitiesPage = activitiesService.getActivitiesByCompanyCode(companyCode, page, size, sortBy, sortDir);
        }

        model.addAttribute("activities", activitiesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", activitiesPage.getTotalPages());
        model.addAttribute("totalItems", activitiesPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);

        model.addAttribute("businessScenariosCount", activitiesService.getDistinctScenariosCount(companyCode));
        model.addAttribute("transactionKeysCount", activitiesService.getDistinctTransactionKeysCount(companyCode));

        Map<String, Boolean> hasChildrenMap = new java.util.HashMap<>();
        for (ScenarioActivities a : activitiesPage.getContent()) {
            hasChildrenMap.put(a.getTransactionKey(), activitiesService.hasChildren(a.getTransactionKey()));
        }
        model.addAttribute("hasChildrenMap", hasChildrenMap);

        return "activitieslist";
    }

    @GetMapping("/api/activities/exists")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkActivityExists(
            @RequestParam String businessScenario,
            @RequestParam String transactionSuffix,
            HttpSession session) {
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty() || 
            businessScenario == null || businessScenario.isEmpty() ||
            transactionSuffix == null || transactionSuffix.isEmpty()) {
            return ResponseEntity.ok(Map.of("exists", false));
        }
        
        List<ScenarioActivities> existingActivities = activitiesService.getActivitiesByCompanyCode(companyCode);
        boolean exists = existingActivities.stream()
            .anyMatch(a -> a.getId().getBusinessScenarioId().getBusinessScenario().equals(businessScenario.trim())
                       && a.getId().getTransactionSuffix().equals(transactionSuffix.trim()));
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping("/ajax/delete/{businessScenario}/{transactionSuffix}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteActivity(@PathVariable String businessScenario, @PathVariable String transactionSuffix, HttpSession session) {
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

            ScenarioActivitiesId id = new ScenarioActivitiesId();
            BusinessScenarioId bsId = new BusinessScenarioId();
            bsId.setCompanyCode(companyCode);
            bsId.setBusinessScenario(businessScenario);
            id.setBusinessScenarioId(bsId);
            id.setTransactionSuffix(transactionSuffix);

            Optional<ScenarioActivities> activityOpt = activitiesService.getActivityById(id);
            if (activityOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Activity not found.");
                return ResponseEntity.ok(response);
            }

            String transactionKey = activityOpt.get().getTransactionKey();
            if (activitiesService.hasChildren(transactionKey)) {
                response.put("success", false);
                response.put("message", "Cannot delete because there are dependent test headers.");
                return ResponseEntity.ok(response);
            }

            activitiesService.deleteActivity(id);
            response.put("success", true);
            response.put("message", "Activity deleted successfully.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error deleting activity: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}