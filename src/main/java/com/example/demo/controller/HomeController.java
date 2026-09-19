package com.example.demo.controller;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.BusinessScenario;
import com.example.demo.entity.TestCaseHeader;
import com.example.demo.entity.TestCaseTransaction;
import com.example.demo.entity.Ticket;
import com.example.demo.entity.TicketTask;
import com.example.demo.repo.TicketRepository;
import com.example.demo.repo.TicketTaskRepository;
import com.example.demo.service.BusinessScenarioService;
import com.example.demo.service.SupportRequestService;
import com.example.demo.service.TestCaseTransactionService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	@Autowired
	private BusinessScenarioService businessScenarioService;
	
	@Autowired
	private TestCaseTransactionService transactionService;
	
	@Autowired
	private SupportRequestService supportRequestService;
	
	@Autowired
	private TicketTaskRepository ticketTaskRepository;

	@Autowired
	private TicketRepository ticketRepository;

    @Autowired
    private com.example.demo.service.UserAccountService userAccountService;

    @Autowired
    private com.example.demo.service.TestCaseHeaderService testCaseHeaderService;

    @Autowired
    private com.example.demo.repo.ProjectRefRepository projectRepo;

    @Autowired
    private com.example.demo.service.DepartmentService departmentService;


    
	@GetMapping("/")
	public String showDashboard() {
	    return "dashboard"; 
	}

	
	@GetMapping("/admin")
	public String showadminDashboard(HttpSession session, Model model) {
		if(session.getAttribute("username") == null) {
			return "redirect:/loginform";
		}
		// Check if user is Admin
		String role = (String) session.getAttribute("role");
		if (!"ADMIN".equals(role)) {
			model.addAttribute("error", "Access denied. This page is only for Administrators.");
			return "redirect:/Menu";
		}
		
		// Add pending support requests count for sidebar badge
		try {
			long pendingCount = supportRequestService.countPendingRequests();
			model.addAttribute("pendingCount", pendingCount);
		} catch (Exception e) {
			model.addAttribute("pendingCount", 0L);
		}
		
		return "admindashboard";
	}


	
	
	
	@GetMapping("/createticket")
	public String showTickets(@RequestParam(required = false) String issueType) {
	    if ("IT_SUPPORT".equalsIgnoreCase(issueType) || "IT Support".equalsIgnoreCase(issueType)) {
	        return "redirect:/api/tickets/createticket?issueType=IT_SUPPORT";
	    }
	    return "redirect:/api/tickets/createticket";
	}
	

    @GetMapping("/addheadertestcase")
    public String showScenarioForm(Model model) {
        model.addAttribute("header", new BusinessScenario());
        return "addheaderscenario";  // Loads form.html inside iframe
    }

 
    @GetMapping("/loginform")
    public String showLoginForm(@RequestParam(value = "concurrent", required = false) String concurrent, Model model) {
        if (concurrent != null) {
            model.addAttribute("error", "Different login detected! System logged you out.");
        }
        return "loginForm";  // Loads form.html inside iframe
    }

    @GetMapping("/api/session/check")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<?> checkSession() {
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("status", "active"));
    }

	

  

    

    @GetMapping("/home")
    public String home(Model model, @ModelAttribute("logoutMessage") String logoutMessage) {
        model.addAttribute("logoutMessage", logoutMessage);
        return "home"; // your home.html view
    }
    
    @GetMapping("/settings")
    public String showSettings(HttpSession session, Model model) {
        // Get user info from session
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        String companyCode = (String) session.getAttribute("companyCode");
        
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("companyCode", companyCode);
        
        return "settings";
    }

 
	/*
	 * @GetMapping("/Menu") public String showQaAdminMenu() { return "qatestermenu";
	 * // Name of the Thymeleaf HTML file without `.html` }
	 * 
	 */
    
   
    
    @GetMapping("/Menu")
    public String showQaAdminMenu(HttpSession session, Model model) {
        if(session.getAttribute("username") == null) {
            session.setAttribute("username", "User");
        }
        if(session.getAttribute("role") == null) {
            session.setAttribute("role", "GUEST");
        }
        String role = (String) session.getAttribute("role");
        String userId = (String) session.getAttribute("userId");
        String companyCode = (String) session.getAttribute("companyCode");
        
        model.addAttribute("userRole", role);
        model.addAttribute("isAdmin", "ADMIN".equalsIgnoreCase(role));
        model.addAttribute("isBusinessManager", "BUSINESS_MANAGER".equalsIgnoreCase(role));
        model.addAttribute("isTester", "TESTER".equalsIgnoreCase(role));
        model.addAttribute("canAddEdit", "ADMIN".equalsIgnoreCase(role) || "BUSINESS_MANAGER".equalsIgnoreCase(role));
        
        // Fetch tasks
        List<TicketTask> tasks;
        String upperRole = (role != null) ? role.toUpperCase() : "GUEST";
        
        if ("ADMIN".equals(upperRole) || "BUSINESS_MANAGER".equals(upperRole)) {
            if (companyCode != null && !companyCode.trim().isEmpty()) {
                tasks = ticketTaskRepository.findByCompanyCode(companyCode.trim());
                // If Admin finds no tasks for the specific company in session, show all tasks
                if (tasks.isEmpty() && "ADMIN".equals(upperRole)) {
                    tasks = ticketTaskRepository.findAll();
                }
            } else {
                tasks = ticketTaskRepository.findAll();
            }
        } else {
            if (userId != null) {
                // For testers, only show tasks assigned to them (case-insensitive)
                tasks = ticketTaskRepository.findByPerformedBy(userId);
                // Also double check with a stream filter just in case the repo method is case-sensitive
                tasks = tasks.stream()
                        .filter(t -> userId.equalsIgnoreCase(t.getPerformedBy()))
                        .collect(java.util.stream.Collectors.toList());
            } else {
                tasks = java.util.Collections.emptyList();
            }
        }
        model.addAttribute("tasks", tasks);
        
        // Add status summary for Admin/Manager
        if ("ADMIN".equalsIgnoreCase(upperRole) || "BUSINESS_MANAGER".equalsIgnoreCase(upperRole)) {
            List<TicketTask> allTasks;
            List<Ticket> allTickets;
            
            if (companyCode != null && !companyCode.trim().isEmpty()) {
                allTasks = ticketTaskRepository.findByCompanyCode(companyCode.trim());
                allTickets = ticketRepository.findByCompanyCode(companyCode.trim());
            } else {
                allTasks = ticketTaskRepository.findAll();
                allTickets = ticketRepository.findAll();
            }
            
            long doneT = allTasks.stream().filter(t -> "CLOSED".equalsIgnoreCase(t.getStatus()) || "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
            long doneK = allTickets.stream().filter(t -> "CLOSED".equalsIgnoreCase(t.getStatus()) || "RESOLVED".equalsIgnoreCase(t.getStatus())).count();
            
            long inProgT = allTasks.stream().filter(t -> t.getStatus() != null && !"CLOSED".equalsIgnoreCase(t.getStatus()) && !"COMPLETED".equalsIgnoreCase(t.getStatus())).count();
            long inProgK = allTickets.stream().filter(t -> t.getStatus() != null && ("OPEN".equalsIgnoreCase(t.getStatus()) || "ASSIGNED".equalsIgnoreCase(t.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(t.getStatus()) || "REOPENED".equalsIgnoreCase(t.getStatus()))).count();
            
            model.addAttribute("allDoneCount", doneT + doneK);
            model.addAttribute("allInProgressCount", inProgT + inProgK);
        }
        
        if ("TESTER".equalsIgnoreCase(role)) {
            return "menu-it-support";
        }
        
        return "qatestermenu";
    }

    @GetMapping("/Menu/ITSupport")
    public String showITSupportMenu(HttpSession session, Model model) {
        if(session.getAttribute("username") == null) {
            return "redirect:/loginform";
        }
        return "menu-it-support";
    }

    @GetMapping("/Menu/QATickets")
    public String showQATicketsMenu(HttpSession session, Model model) {
        if(session.getAttribute("username") == null) {
            return "redirect:/loginform";
        }
        return "menu-qa-tickets";
    }

    @GetMapping("/Menu/BusinessScenarios")
    public String showBusinessScenariosMenu(HttpSession session, Model model) {
        if(session.getAttribute("username") == null) {
            return "redirect:/loginform";
        }
        return "menu-business-scenarios";
    }

    @GetMapping("/Menu/BusinessScenarioActivities")
    public String showBusinessScenarioActivitiesMenu(HttpSession session, Model model) {
        if(session.getAttribute("username") == null) {
            return "redirect:/loginform";
        }
        return "menu-scenario-activities";
    }

    @GetMapping("/Menu/TestCases")
    public String showTestCasesMenu(HttpSession session, Model model) {
        if(session.getAttribute("username") == null) {
            return "redirect:/loginform";
        }
        return "menu-test-cases";
    }

    @GetMapping("/Menu/QualityControl")
    public String showQualityControlMenu(HttpSession session, Model model) {
        if(session.getAttribute("username") == null) {
            return "redirect:/loginform";
        }
        return "menu-quality-control";
    }

    @GetMapping("/Menu/ScrumBoard")
    public String showScrumBoardMenu(HttpSession session, Model model) {
        System.out.println("DEBUG: /Menu/ScrumBoard requested. Redirecting to board...");
        if(session.getAttribute("username") == null) {
            return "redirect:/loginform";
        }
        return "redirect:/api/tickets/board";
    }

    // Diagnostic route to check if controller is updated
    @GetMapping("/api/check-board-status")
    @org.springframework.web.bind.annotation.ResponseBody
    public String checkBoardStatus() {
        return "Scrum Board Controller is ACTIVE and UPDATED";
    }
    
    @GetMapping("/modules-list")
    public String showModulesList(HttpSession session, Model model) {
        // Check if user is logged in
        if (session.getAttribute("username") == null && session.getAttribute("userId") == null) {
            return "redirect:/loginform";
        }
        
        String role = (String) session.getAttribute("role");
        if (role == null) {
            role = "GUEST";
        }
        
        model.addAttribute("userRole", role);
        model.addAttribute("canAddEdit", "ADMIN".equals(role) || "BUSINESS_MANAGER".equals(role));
        
        return "modules-list";
    }
    
    @GetMapping("/business-manager/users")
    public String showBusinessManagerUsers(@RequestParam(required = false) String department, HttpSession session, Model model) {
        if (session.getAttribute("username") == null) {
            return "redirect:/loginform";
        }
        String role = (String) session.getAttribute("role");
        if (!"BUSINESS_MANAGER".equals(role) && !"ADMIN".equals(role)) {
            return "redirect:/Menu";
        }
        String companyCode = (String) session.getAttribute("companyCode");
        java.util.List<com.example.demo.entity.UserAccount> users;
        if (companyCode != null) {
            users = userAccountService.getBusinessManagersAndTestersByCompanyCode(companyCode);
        } else {
            users = userAccountService.getAllUsers();
        }

        List<com.example.demo.entity.Department> departments = companyCode != null ? departmentService.getByCompanyCode(companyCode) : departmentService.getAll();

        if (department != null && !department.trim().isEmpty() && !"ALL".equalsIgnoreCase(department)) {
            final String targetDept = department.trim();
            users = users.stream()
                         .filter(u -> isDeptMatch(u.getDepartment(), targetDept, departments))
                         .collect(java.util.stream.Collectors.toList());
        }

        model.addAttribute("users", users);
        model.addAttribute("selectedDepartment", department != null ? department : "ALL");
        model.addAttribute("departments", departments);
        return "businessmanagerusers";
    }

    private boolean isDeptMatch(String userDept, String targetDept, List<com.example.demo.entity.Department> deptList) {
        if (targetDept == null || targetDept.trim().isEmpty() || "ALL".equalsIgnoreCase(targetDept)) {
            return true;
        }
        if (userDept == null || userDept.trim().isEmpty()) {
            return false;
        }
        String uLower = userDept.trim().toLowerCase();
        String tLower = targetDept.trim().toLowerCase();

        if (uLower.equals(tLower) || uLower.contains(tLower) || tLower.contains(uLower)) {
            return true;
        }

        if (deptList != null) {
            for (com.example.demo.entity.Department d : deptList) {
                String code = d.getDepartmentCode() != null ? d.getDepartmentCode().trim().toLowerCase() : "";
                String name = d.getDepartmentName() != null ? d.getDepartmentName().trim().toLowerCase() : "";

                boolean targetMatchesDept = (!code.isEmpty() && (tLower.equals(code) || tLower.contains(code))) ||
                                            (!name.isEmpty() && (tLower.equals(name) || tLower.contains(name)));

                boolean userMatchesDept = (!code.isEmpty() && (uLower.equals(code) || uLower.contains(code))) ||
                                          (!name.isEmpty() && (uLower.equals(name) || uLower.contains(name)));

                if (targetMatchesDept && userMatchesDept) {
                    return true;
                }
            }
        }
        return false;
    }

    @GetMapping("/business-manager")
    public String showBusinessManagerDashboard(HttpSession session, Model model) {
        // Check if user is logged in
        if(session.getAttribute("username") == null) {
            return "redirect:/loginform";
        }
        
        // Check if user is a Business Manager
        String role = (String) session.getAttribute("role");
        if (!"BUSINESS_MANAGER".equals(role)) {
            model.addAttribute("error", "Access denied. This page is only for Business Managers.");
            return "redirect:/Menu";
        }
        
        if(session.getAttribute("username") == null) {
            session.setAttribute("username", "User");
        }
        
        // Get company code from session
        String companyCode = (String) session.getAttribute("companyCode");
        
        model.addAttribute("userRole", role);
        model.addAttribute("isAdmin", false);
        model.addAttribute("isBusinessManager", true);
        model.addAttribute("isTester", false);
        model.addAttribute("canAddEdit", true);
        model.addAttribute("companyCode", companyCode);
        
        return "businessmanagerdashboard";
    }

    
    
    
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();  // clear session
        redirectAttributes.addFlashAttribute("logoutMessage", "You have successfully logged out!");
        return "redirect:/";  // redirect to landing page
    }

    @GetMapping("/api/all-transactions")
    public String listAllTransactions(HttpSession session, Model model) {
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "Company Code not found in session.");
            return "redirect:/Menu";
        }

        // Get all transactions for this company
        List<TestCaseTransaction> transactions = transactionService.getByCompanyCode(companyCode);

        model.addAttribute("transactions", transactions);
        model.addAttribute("totalItems", transactions.size());
        model.addAttribute("testCaseHeader", null); // No specific header - showing all
        model.addAttribute("companyCode", companyCode);

        // Add metrics for dashboard boxes
        model.addAttribute("passCount", transactionService.getPassCount(companyCode));
        model.addAttribute("failCount", transactionService.getFailCount(companyCode));
        model.addAttribute("pendingCount", transactionService.getPendingCount(companyCode));

        return "listtransactions";
    }
    
    @GetMapping("/api/transactions/view-transaction/{id}")
    public String viewTransaction(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("userId") == null && session.getAttribute("username") == null) {
            return "redirect:/loginform";
        }

        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Company Code not found in session.");
            return "redirect:/api/all-transactions";
        }
        
        Optional<TestCaseTransaction> transactionOpt = transactionService.findById(id);
        if (transactionOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Transaction not found.");
            return "redirect:/api/all-transactions";
        }
        
        TestCaseTransaction transaction = transactionOpt.get();
        
        // Verify transaction belongs to the company
        if (!companyCode.equals(transaction.getCompanyCode())) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to view this transaction.");
            return "redirect:/api/all-transactions";
        }
        
        model.addAttribute("transaction", transaction);
        model.addAttribute("companyCode", companyCode);
        
        // Add role information for template
        String role = (String) session.getAttribute("role");
        model.addAttribute("userRole", role);
        model.addAttribute("canAddEdit", "ADMIN".equals(role) || "BUSINESS_MANAGER".equals(role));
        
        return "view-transaction";
    }
    
    @GetMapping("/api/transactions/edit-transaction/{id}")
    public String showEditTransactionForm(@PathVariable Long id, HttpSession session, Model model) {
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "Company Code not found in session.");
            return "redirect:/Menu";
        }
        
        Optional<TestCaseTransaction> transactionOpt = transactionService.findById(id);
        if (transactionOpt.isEmpty()) {
            model.addAttribute("error", "Transaction not found.");
            return "redirect:/api/all-transactions";
        }
        
        TestCaseTransaction transaction = transactionOpt.get();
        
        // Verify transaction belongs to the company
        if (!companyCode.equals(transaction.getCompanyCode())) {
            model.addAttribute("error", "You do not have permission to edit this transaction.");
            return "redirect:/api/all-transactions";
        }

        // Populate transient combinedKey
        Optional<com.example.demo.entity.TestCaseHeader> headerOpt = testCaseHeaderService.findById(transaction.getTestcaseHeaderId());
        headerOpt.ifPresent(h -> transaction.setCombinedKey(h.getCombinedKey()));
        
        model.addAttribute("transaction", transaction);
        model.addAttribute("companyCode", companyCode);
        model.addAttribute("editMode", true);
        return "addtransactionform";
    }
    
    @PostMapping("/editTransactionform")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> updateTransaction(
            @ModelAttribute("transaction") TestCaseTransaction transaction,
            @RequestParam(value = "screenshotFiles", required = false) MultipartFile[] files,
            @RequestParam(value = "deleteImages", required = false) List<String> deleteImages,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            String companyCode = (String) session.getAttribute("companyCode");
            if (companyCode == null || companyCode.isEmpty()) {
                response.put("success", false);
                response.put("message", "Company Code not found in session. Please log in again.");
                return org.springframework.http.ResponseEntity.ok(response);
            }
            
            // Verify transaction exists and belongs to company
            if (transaction.getId() == null) {
                response.put("success", false);
                response.put("message", "Transaction ID is required.");
                return org.springframework.http.ResponseEntity.ok(response);
            }
            
            Optional<TestCaseTransaction> existingOpt = transactionService.findById(transaction.getId());
            if (existingOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Transaction not found.");
                return org.springframework.http.ResponseEntity.ok(response);
            }
            
            TestCaseTransaction existing = existingOpt.get();
            if (!companyCode.equals(existing.getCompanyCode())) {
                response.put("success", false);
                response.put("message", "You do not have permission to edit this transaction.");
                return org.springframework.http.ResponseEntity.ok(response);
            }

            // Populate transient combinedKey from header to prevent null01 mainKey bug
            Optional<com.example.demo.entity.TestCaseHeader> headerOpt = testCaseHeaderService.findById(existing.getTestcaseHeaderId());
            headerOpt.ifPresent(h -> transaction.setCombinedKey(h.getCombinedKey()));
            
            // Check duplicate step number (edit mode)
            if (transaction.getCombinedKey() != null && transaction.getStepNo() != null) {
                String fullMainKey = transaction.getCombinedKey().trim() + transaction.getStepNo().trim();
                List<TestCaseTransaction> existingMatches = transactionService.getByMainKey(fullMainKey);
                boolean exists = existingMatches.stream()
                        .anyMatch(t -> companyCode.equals(t.getCompanyCode()) && !t.getId().equals(transaction.getId()));
                if (exists) {
                    response.put("success", false);
                    response.put("message", "Transaction with this step number already exists for this test case.");
                    return org.springframework.http.ResponseEntity.ok(response);
                }
            }
            
            String existingPathsStr = existing.getScreenshotPaths();
            String uploadDir = Paths.get("uploads", "screenshots").toAbsolutePath().toString();
            
            // 1. Process deleteImages
            if (deleteImages != null && !deleteImages.isEmpty() && existingPathsStr != null) {
                for (String delImg : deleteImages) {
                    try {
                        File fileToDelete = new File(uploadDir, delImg.trim());
                        if (fileToDelete.exists()) {
                            fileToDelete.delete();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            // 2. Filter remaining screenshot paths
            List<String> newPaths = new java.util.ArrayList<>();
            if (existingPathsStr != null && !existingPathsStr.isEmpty()) {
                String[] split = existingPathsStr.split(",");
                for (String path : split) {
                    String trimmed = path.trim();
                    String cleanName = trimmed;
                    int lastSlashIdx = Math.max(trimmed.lastIndexOf('/'), trimmed.lastIndexOf('\\'));
                    if (lastSlashIdx >= 0) {
                        cleanName = trimmed.substring(lastSlashIdx + 1);
                    }
                    if (deleteImages == null || !deleteImages.contains(cleanName)) {
                        newPaths.add(trimmed);
                    }
                }
            }
            
            // 3. Process newly uploaded files
            if (files != null && files.length > 0) {
                new File(uploadDir).mkdirs();
                for (MultipartFile file : files) {
                    if (file != null && !file.isEmpty()) {
                        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                        File destination = new File(uploadDir, fileName);
                        file.transferTo(destination);
                        newPaths.add("/uploads/screenshots/" + fileName);
                    }
                }
            }
            
            if (!newPaths.isEmpty()) {
                transaction.setScreenshotPaths(String.join(",", newPaths));
            } else {
                transaction.setScreenshotPaths(null);
            }
            
            // Ensure company code and header ID are preserved
            transaction.setCompanyCode(companyCode);
            transaction.setTestcaseHeaderId(existing.getTestcaseHeaderId());
            
            // Save the transaction
            transactionService.save(transaction);
            
            response.put("success", true);
            response.put("message", "Transaction updated successfully!");
            return org.springframework.http.ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error updating transaction: " + e.getMessage());
            return org.springframework.http.ResponseEntity.ok(response);
        }
    }



//    
//    
////    Scenario saction Methods
//    
////
////    @GetMapping("/scenariotralist")
////    public String getAllTransactions(Model model) {
////        model.addAttribute("scenarios", scenarioTransactionservice.getAllScenarios());
////        return "scenariotransactionlist"; // Thymeleaf template name
////    }
////
////    
//    
//    
//    @GetMapping("/addscenariotransaction")
//    public String showForm(Model model) {
//        model.addAttribute("scenario", new Scenariotransaction());
//        return "addscenariotransaction";
//    }
//    
//    
////testheader
//    
//    @GetMapping("/addtestheader")
//    public String showAddForm(Model model) {
//        model.addAttribute("testcase", new TestCaseHeader());
//        return "addtestheader";
//    }
//    
//    @GetMapping("/listtestheader")
//    public String showListForm(Model model) {
//        model.addAttribute("testcase", new TestCaseHeader());
//        return "testheaderlist";
//    }

    @GetMapping("/api/dashboard/manager-stats")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, Object> getManagerStats(HttpSession session) {
        String companyCode = (String) session.getAttribute("companyCode");
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        long teamCount = 0;
        long openTickets = 0;
        long pendingTasks = 0;
        long completedTests = 0;
        
        if (companyCode != null && !companyCode.trim().isEmpty()) {
            teamCount = userAccountService.getUsersByCompanyCode(companyCode).size();
            openTickets = ticketRepository.countByCompanyCodeAndStatus(companyCode, "OPEN");
            pendingTasks = ticketTaskRepository.countInProgressByCompany(companyCode);
            completedTests = testCaseHeaderService.getByCompanyCode(companyCode, 0, 1, "id", "desc").getTotalElements();
        }
        
        if (teamCount == 0 && openTickets == 0 && pendingTasks == 0 && completedTests == 0) {
            teamCount = userAccountService.getAllUsers().size();
            openTickets = ticketRepository.findAll().stream().filter(t -> "OPEN".equalsIgnoreCase(t.getStatus())).count();
            pendingTasks = ticketTaskRepository.findAll().stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus()) || "Pending".equalsIgnoreCase(t.getStatus())).count();
            completedTests = testCaseHeaderService.getAll(0, 1, "id", "desc").getTotalElements();
        }

        stats.put("teamCount", teamCount);
        stats.put("openTickets", openTickets);
        stats.put("pendingTasks", pendingTasks);
        stats.put("completedTests", completedTests);
        
        return stats;
    }
}
