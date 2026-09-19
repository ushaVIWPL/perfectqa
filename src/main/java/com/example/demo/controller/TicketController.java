package com.example.demo.controller;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.ApplicationRef;
import com.example.demo.entity.Department;
import com.example.demo.entity.ModuleRef;
import com.example.demo.entity.ProjectRef;
import com.example.demo.entity.TestCaseHeader;
import com.example.demo.entity.TestCaseTransaction;
import com.example.demo.entity.Ticket;
import com.example.demo.entity.TicketComment;
import com.example.demo.entity.TicketHistory;
import com.example.demo.entity.TicketTask;
import com.example.demo.entity.UserAccount;
import com.example.demo.repo.Applicationrefrepo;
import com.example.demo.repo.ModuleRefRepository;
import com.example.demo.repo.ProjectRefRepository;
import com.example.demo.repo.SupportIssueTypeRepository;
import com.example.demo.repo.TaskTypeRepository;
import com.example.demo.repo.TestCaseHeaderRepository;
import com.example.demo.repo.TicketTaskRepository;
import com.example.demo.service.DepartmentService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.ReportExportService;
import com.example.demo.service.TestCaseTransactionService;
import com.example.demo.service.TicketService;
import com.example.demo.service.UserAccountService;
import com.lowagie.text.DocumentException;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final TestCaseTransactionService transactionService;
    private final UserAccountService userAccountService;
    private final ProjectRefRepository projectRepository;
    private final ModuleRefRepository moduleRepository;
    private final Applicationrefrepo applicationRepository;
    private final TestCaseHeaderRepository testCaseHeaderRepository;
    private final TicketTaskRepository ticketTaskRepository;
    private final TaskTypeRepository taskTypeRepository;
    private final SupportIssueTypeRepository supportIssueTypeRepository;
    private final NotificationService notificationService;
    private final ReportExportService reportExportService;
    private final com.example.demo.service.DepartmentService departmentService;

    // Show ticket form for a specific transaction
    @GetMapping("/raise/{transactionId}")
    public String showTicketFormForTransaction(
            @PathVariable Long transactionId,
            HttpSession session,
            Model model) {
        
        Optional<TestCaseTransaction> transactionOpt = transactionService.findById(transactionId);
        
        if (transactionOpt.isEmpty()) {
            model.addAttribute("error", "Transaction not found");
            return "redirect:/api/all-transactions";
        }
        
        TestCaseTransaction transaction = transactionOpt.get();
        
        Ticket ticket = new Ticket();
        ticket.setTransactionId(transactionId);
        ticket.setTransactionMainKey(transaction.getMainKey());
        ticket.setCompanyCode(transaction.getCompanyCode());
        ticket.setTestCaseTransactionCode(transaction.getMainKey());
        ticket.setAssigneeUserCode("philip@iwpl.org"); // Auto-assign for Triage Purposes
        
        // Try to get test case code from the transaction's main key
        // MainKey format is typically: transactionKey + testCaseNo + stepNo
        if (transaction.getMainKey() != null && transaction.getMainKey().length() >= 4) {
            // Extract test case combined key (first part of mainKey before stepNo)
            String mainKey = transaction.getMainKey();
            // Combined key is usually the first 6 chars (4 transaction + 2 testcase)
            if (mainKey.length() >= 6) {
                ticket.setTestCaseCode(mainKey.substring(0, 6));
            }
        }
        
        String username = (String) session.getAttribute("username");
        if (username != null) {
            ticket.setUserCode(username);
        }
        
        // Get company users for assignment dropdown
        String companyCode = transaction.getCompanyCode();
        if (companyCode != null && !companyCode.isEmpty()) {
            List<UserAccount> companyUsers = userAccountService.getUsersByCompanyCode(companyCode);
            model.addAttribute("companyUsers", companyUsers);
            
            // Get applications for this company (through projects)
            List<ProjectRef> projects = projectRepository.findByCompanyCode(companyCode);
            if (!projects.isEmpty()) {
                // Get all applications from all projects
                List<ApplicationRef> applications = new java.util.ArrayList<>();
                for (ProjectRef project : projects) {
                    applications.addAll(applicationRepository.findByProjectCode(project.getProjectCode()));
                }
                model.addAttribute("applications", applications);
            }
            
            // Get all modules
            List<ModuleRef> modules = moduleRepository.findAll();
            model.addAttribute("modules", modules);
            
            // Get all test cases for this company
            List<TestCaseHeader> testCases = testCaseHeaderRepository.findByCompanyCode(companyCode);
            model.addAttribute("testCases", testCases);
        }
        
        model.addAttribute("ticket", ticket);
        model.addAttribute("transaction", transaction);
        model.addAttribute("isEdit", false);
        
        return "ticket-form-qa";
    }
    
    // Show empty ticket form
    @GetMapping("/createticket")
    public String createTicket(@RequestParam(required = false) String issueType, HttpSession session, Model model) {
        Ticket ticket = new Ticket();
        
        String companyCode = (String) session.getAttribute("companyCode");
        ticket.setAssigneeUserCode("philip@iwpl.org"); // Auto-assign for Triage Purposes
        
        if (issueType != null && !issueType.isEmpty()) {
            ticket.setIssueType(issueType);
        }
        
        if (companyCode != null) {
            ticket.setCompanyCode(companyCode);
            // Get company users for assignment dropdown
            List<UserAccount> companyUsers = userAccountService.getUsersByCompanyCode(companyCode);
            model.addAttribute("companyUsers", companyUsers);
            
            // Get applications for this company (through projects)
            List<ProjectRef> projects = projectRepository.findByCompanyCode(companyCode);
            if (!projects.isEmpty()) {
                List<ApplicationRef> applications = new java.util.ArrayList<>();
                for (ProjectRef project : projects) {
                    applications.addAll(applicationRepository.findByProjectCode(project.getProjectCode()));
                }
                model.addAttribute("applications", applications);
            }
            
            // Get all modules for this company
            List<ModuleRef> modules = moduleRepository.findAll();
            model.addAttribute("modules", modules);
            
            // Get all test cases for this company
            List<TestCaseHeader> testCases = testCaseHeaderRepository.findByCompanyCode(companyCode);
            model.addAttribute("testCases", testCases);
        }
        
        String username = (String) session.getAttribute("username");
        if (username != null) {
            ticket.setUserCode(username);
        }
        
        model.addAttribute("ticket", ticket);
        model.addAttribute("transaction", null);
        model.addAttribute("isEdit", false);
        
        if ("IT_SUPPORT".equalsIgnoreCase(issueType) || "IT Support".equalsIgnoreCase(issueType)) {
            model.addAttribute("supportIssues", supportIssueTypeRepository.findAll());
            return "ticket-form-support";
        }
        return "ticket-form-qa";
    }
    
    // AJAX endpoint to get modules by project
    @GetMapping("/modules-by-project/{projectCode}")
    @ResponseBody
    public List<ModuleRef> getModulesByProject(@PathVariable String projectCode) {
        return moduleRepository.findByProjectCode(projectCode);
    }
    
    // AJAX endpoint to get modules by application
    @GetMapping("/modules-by-app/{appCode}")
    @ResponseBody
    public List<ModuleRef> getModulesByApp(@PathVariable String appCode) {
        return moduleRepository.findByApplicationCode(appCode);
    }
    
    // AJAX endpoint to get test cases by company
    @GetMapping("/testcases-by-company/{companyCode}")
    @ResponseBody
    public List<TestCaseHeader> getTestCasesByCompany(@PathVariable String companyCode) {
        return testCaseHeaderRepository.findByCompanyCode(companyCode);
    }
    
    // AJAX endpoint to get all modules
    @GetMapping("/all-modules")
    @ResponseBody
    public List<ModuleRef> getAllModules() {
        return moduleRepository.findAll();
    }
    
    // Helper method to save attachment files prefixed with ticket name/number instead of random UUID
    private String buildTicketAttachmentFilename(String ticketNo, String originalFilename, String uploadDir) {
        String safeTicketNo = (ticketNo != null && !ticketNo.trim().isEmpty())
                ? ticketNo.trim().replaceAll("[^a-zA-Z0-9_-]", "_")
                : "TKT";
        String cleanOriginalName = (originalFilename != null && !originalFilename.trim().isEmpty())
                ? originalFilename.replaceAll("[\\\\/:*?\"<>|]", "_")
                : "attachment";

        String candidate = safeTicketNo + "_" + cleanOriginalName;
        File file = new File(uploadDir, candidate);
        if (!file.exists()) {
            return candidate;
        }
        int dotIdx = cleanOriginalName.lastIndexOf('.');
        String baseName = dotIdx != -1 ? cleanOriginalName.substring(0, dotIdx) : cleanOriginalName;
        String ext = dotIdx != -1 ? cleanOriginalName.substring(dotIdx) : "";
        int counter = 1;
        while (file.exists()) {
            candidate = safeTicketNo + "_" + baseName + "_" + counter + ext;
            file = new File(uploadDir, candidate);
            counter++;
        }
        return candidate;
    }

    @PostMapping("/save")
    public String saveTicket(
            @ModelAttribute("ticket") Ticket ticket,
            @RequestParam(value = "attachmentFiles", required = false) MultipartFile[] files,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        System.out.println("=== TicketController.saveTicket CALLED ===");
        System.out.println("Ticket object from form: " + (ticket != null ? ticket.getIssueDescription() : "NULL"));
        System.out.println("Company: " + (ticket != null ? ticket.getCompanyCode() : "N/A"));
        System.out.println("Assignee: " + (ticket != null ? ticket.getAssigneeUserCode() : "N/A"));

        try {
            String username = (String) session.getAttribute("userId"); // Changed from "username" to "userId" for consistency
            if (username == null) username = (String) session.getAttribute("username");
            if (username == null) username = "System";
            
            System.out.println("Submitted by session user: " + username);
            
            if (ticket.getTicketNo() == null || ticket.getTicketNo().trim().isEmpty()) {
                ticket.setTicketNo(ticketService.generateTicketNo());
            }

            // Handle file uploads
            if (files != null && files.length > 0) {
                StringBuilder paths = new StringBuilder();
                String uploadDir = Paths.get("uploads", "tickets").toAbsolutePath().toString();
                new File(uploadDir).mkdirs();
                
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String fileName = buildTicketAttachmentFilename(ticket.getTicketNo(), file.getOriginalFilename(), uploadDir);
                        File destination = new File(uploadDir, fileName);
                        file.transferTo(destination);
                        
                        if (paths.length() > 0) paths.append(",");
                        paths.append(fileName);
                    }
                }
                
                if (paths.length() > 0) {
                    ticket.setAttachments(paths.toString());
                }
            }
            
            // Set company code from session if not set
            if (ticket.getCompanyCode() == null || ticket.getCompanyCode().isEmpty()) {
                String companyCode = (String) session.getAttribute("companyCode");
                if (companyCode != null) {
                    ticket.setCompanyCode(companyCode);
                }
            }
            
            // Set userCode from session if not set
            if (ticket.getUserCode() == null || ticket.getUserCode().isEmpty()) {
                ticket.setUserCode(username);
            }
            
            // Standardize issueType for consistency
            if (ticket.getIssueType() != null) {
                String type = ticket.getIssueType().trim();
                if (type.equalsIgnoreCase("IT Support") || type.equalsIgnoreCase("IT_SUPPORT")) {
                    ticket.setIssueType("IT_SUPPORT");
                }
            }
            
            Ticket savedTicket = ticketService.createTicket(ticket, username);

            String emailWarning = ticketService.consumeLastCreateEmailWarning();
            if (emailWarning != null) {
                redirectAttributes.addFlashAttribute("warning", emailWarning);
            }
            
            redirectAttributes.addFlashAttribute("success", 
                "Ticket " + savedTicket.getTicketNo() + " created successfully!"
                    + (emailWarning == null && savedTicket.getAssigneeUserCode() != null
                        && !savedTicket.getAssigneeUserCode().isBlank()
                        ? " Assignee notified by email." : ""));
            
            // Redirect based on type
            if ("IT_SUPPORT".equalsIgnoreCase(savedTicket.getIssueType())) {
                return "redirect:/api/tickets/list?issueType=IT_SUPPORT";
            }
            
            return "redirect:/api/tickets/list";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create ticket: " + e.getMessage());
            if (ticket != null && "IT_SUPPORT".equalsIgnoreCase(ticket.getIssueType())) {
                return "redirect:/api/tickets/createticket?issueType=IT_SUPPORT";
            }
            return "redirect:/api/tickets/createticket";
        }
    }
    
    // List all tickets
    @GetMapping("/list")
    public String listTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String issueType,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session, 
            Model model) {
        
        String companyCode = (String) session.getAttribute("companyCode");
        String role = (String) session.getAttribute("role");
        String userId = (String) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        String email = (String) session.getAttribute("email");
        String userDepartment = (String) session.getAttribute("department");
        
        if (userDepartment == null || userDepartment.trim().isEmpty()) {
            UserAccount user = null;
            if (userId != null) {
                user = userAccountService.findByUserId(userId);
            }
            if (user == null && email != null) {
                user = userAccountService.findByUserId(email);
            }
            if (user == null && username != null) {
                user = userAccountService.findByUserId(username);
            }
            if (user != null) {
                userDepartment = user.getDepartment();
                if (userDepartment != null) {
                    session.setAttribute("department", userDepartment);
                }
            }
        }
        
        // Filter department logic: Non-managers/non-admins (testers, sales, finance, etc.) see ONLY their department's tickets
        boolean isManagerOrAdmin = "ADMIN".equals(role) || "MANAGER".equals(role) || "BUSINESS_MANAGER".equals(role);
        String filterDepartment = department; 
        if (!isManagerOrAdmin) {
            filterDepartment = userDepartment; // Enforce user's department for testers and team members
        }

        // Retrieve base tickets list based on issueType or company
        List<Ticket> baseTickets;
        if (issueType != null && !issueType.isEmpty()) {
            baseTickets = ticketService.getTicketsByIssueType(companyCode, issueType);
        } else if (status != null && !status.isEmpty()) {
            baseTickets = ticketService.getByStatus(status);
            // If not admin, also filter by company
            if (!"ADMIN".equals(role) && companyCode != null) {
                baseTickets = baseTickets.stream()
                    .filter(t -> companyCode.equals(t.getCompanyCode()))
                    .collect(java.util.stream.Collectors.toList());
            }
        } else if (companyCode != null && !companyCode.isEmpty() && !"ADMIN".equals(role)) {
            baseTickets = ticketService.getByCompanyCode(companyCode);
        } else {
            baseTickets = ticketService.getAllTickets();
        }

        // Compute statistics on the unfiltered base list
        long openCount = baseTickets.stream().filter(t -> "OPEN".equalsIgnoreCase(t.getStatus())).count();
        long assignedCount = baseTickets.stream().filter(t -> "ASSIGNED".equalsIgnoreCase(t.getStatus())).count();
        long inProgressCount = baseTickets.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
        long resolvedCount = baseTickets.stream().filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus())).count();
        long closedCount = baseTickets.stream().filter(t -> "CLOSED".equalsIgnoreCase(t.getStatus())).count();
        
        // Apply status filter if needed for display
        List<Ticket> tickets = baseTickets;
        if (status != null && !status.isEmpty()) {
            tickets = tickets.stream()
                .filter(t -> status.equalsIgnoreCase(t.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        }

        // Apply department filter if needed for display
        List<Department> allDeptsForFilter = departmentService.getAll();
        if (filterDepartment != null && !filterDepartment.trim().isEmpty() && !"ALL".equalsIgnoreCase(filterDepartment)) {
            final String finalFilterDept = filterDepartment;
            tickets = tickets.stream()
                .filter(t -> isDeptMatch(t.getDepartmentCode(), finalFilterDept, allDeptsForFilter))
                .collect(java.util.stream.Collectors.toList());
        }
        // Remove IT_SUPPORT from generic list when not in support flow
        if (!"IT_SUPPORT".equalsIgnoreCase(issueType) && !"IT Support".equalsIgnoreCase(issueType)) {
            tickets = tickets.stream()
                .filter(t -> t.getIssueType() == null || (!t.getIssueType().equalsIgnoreCase("IT_SUPPORT") && !t.getIssueType().equalsIgnoreCase("IT Support")))
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (createdBy != null && !createdBy.isEmpty()) {
            tickets = tickets.stream()
                .filter(t -> createdBy.equalsIgnoreCase(t.getUserCode()))
                .collect(java.util.stream.Collectors.toList());
        }
        if (assignee != null && !assignee.isEmpty()) {
            tickets = tickets.stream()
                .filter(t -> assignee.equalsIgnoreCase(t.getAssigneeUserCode()))
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            List<Department> deptsForSearch = departmentService.getAll();
            tickets = tickets.stream()
                .filter(t -> (t.getTicketNo() != null && t.getTicketNo().toLowerCase().contains(q))
                          || (t.getIssueDescription() != null && t.getIssueDescription().toLowerCase().contains(q))
                          || (t.getSummary() != null && t.getSummary().toLowerCase().contains(q))
                          || (t.getDescription() != null && t.getDescription().toLowerCase().contains(q))
                          || (t.getIssueType() != null && t.getIssueType().toLowerCase().contains(q))
                          || (t.getDepartmentCode() != null && t.getDepartmentCode().toLowerCase().contains(q))
                          || isDeptMatch(t.getDepartmentCode(), q, deptsForSearch))
                .collect(java.util.stream.Collectors.toList());
        }

        int totalItems = tickets.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalItems);
        
        List<Ticket> paginatedTickets = new java.util.ArrayList<>();
        if (fromIndex < totalItems) {
            paginatedTickets = tickets.subList(fromIndex, toIndex);
        }

        // Map assignee user IDs to their respective formatted roles
        java.util.Map<String, String> assigneeRoles = new java.util.HashMap<>();
        if (paginatedTickets != null) {
            for (Ticket t : paginatedTickets) {
                String assigneeCode = t.getAssigneeUserCode();
                if (assigneeCode != null && !assigneeCode.trim().isEmpty() && !assigneeRoles.containsKey(assigneeCode)) {
                    UserAccount user = userAccountService.findByUserId(assigneeCode);
                    if (user != null && user.getUserRole() != null) {
                        assigneeRoles.put(assigneeCode, formatRole(user.getUserRole()));
                    }
                }
            }
        }
        model.addAttribute("assigneeRoles", assigneeRoles);

        // Map creator user IDs to their respective formatted names
        java.util.Map<String, String> creatorNames = new java.util.HashMap<>();
        if (paginatedTickets != null) {
            for (Ticket t : paginatedTickets) {
                String creatorCode = t.getUserCode();
                if (creatorCode != null && !creatorCode.trim().isEmpty() && !creatorNames.containsKey(creatorCode)) {
                    UserAccount user = userAccountService.findByUserId(creatorCode);
                    if (user != null) {
                        String fullName = "";
                        if (user.getFirstName() != null) fullName += user.getFirstName();
                        if (user.getLastName() != null) {
                            if (!fullName.isEmpty()) fullName += " ";
                            fullName += user.getLastName();
                        }
                        creatorNames.put(creatorCode, fullName.isEmpty() ? creatorCode : fullName);
                    } else {
                        creatorNames.put(creatorCode, creatorCode);
                    }
                }
            }
        }
        model.addAttribute("creatorNames", creatorNames);

        // Add stats to model
        model.addAttribute("openCount", openCount);
        model.addAttribute("assignedCount", assignedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("closedCount", closedCount);
        
        // Determine if we are in IT Support flow or QA flow
        boolean isSupportFlow = "IT_SUPPORT".equalsIgnoreCase(issueType) || "IT Support".equalsIgnoreCase(issueType);
        
        List<UserAccount> companyUsers = null;
        if (companyCode != null && !companyCode.isEmpty()) {
            companyUsers = userAccountService.getUsersByCompanyCode(companyCode);
        } else {
            companyUsers = userAccountService.getAllUsers();
        }
        model.addAttribute("companyUsers", companyUsers);
        model.addAttribute("selectedCreatedBy", createdBy);
        model.addAttribute("selectedAssignee", assignee);
        
        if (companyCode != null && !companyCode.isEmpty()) {
            model.addAttribute("departments", departmentService.getByCompanyCode(companyCode));
        } else {
            model.addAttribute("departments", departmentService.getAll());
        }
        model.addAttribute("selectedDepartment", department);

        Map<String, String> departmentMap = new java.util.HashMap<>();
        List<com.example.demo.entity.Department> allDeptsForMap = departmentService.getAll();
        if (allDeptsForMap != null) {
            for (com.example.demo.entity.Department d : allDeptsForMap) {
                if (d.getDepartmentCode() != null) {
                    departmentMap.put(d.getDepartmentCode(), d.getDepartmentName());
                    if (d.getDepartmentName() != null) {
                        departmentMap.put(d.getDepartmentName(), d.getDepartmentName());
                    }
                }
            }
        }
        model.addAttribute("departmentMap", departmentMap);
        
        model.addAttribute("tickets", paginatedTickets);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedIssueType", issueType);
        model.addAttribute("isAdmin", "ADMIN".equals(role));
        model.addAttribute("isSupportFlow", isSupportFlow);

        // Pagination metadata
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);
        model.addAttribute("search", search);
        
        // Return specific list view based on flow
        if (isSupportFlow) {
            return "ticket-list-support";
        }
        return "ticket-list-qa";
    }

    // ==================== SCRUM BOARD ====================

    @GetMapping("/board")
    public String getScrumBoard(
            @RequestParam(required = false) String issueType,
            HttpSession session, 
            Model model) {
        
        System.out.println("DEBUG: getScrumBoard called. IssueType: " + issueType);
        
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            System.err.println("DEBUG: No companyCode in session. Redirecting to login.");
            return "redirect:/loginform";
        }

        System.out.println("DEBUG: Fetching tickets for company: " + companyCode);
        List<Ticket> allTickets = ticketService.getByCompanyCode(companyCode);
        
        if (allTickets == null) {
            System.err.println("DEBUG: ticketService returned null list. Initializing empty list.");
            allTickets = new java.util.ArrayList<>();
        }

        // Filter by issue type (Standard vs IT Support)
        if ("IT_SUPPORT".equalsIgnoreCase(issueType)) {
            allTickets = allTickets.stream()
                .filter(t -> "IT_SUPPORT".equalsIgnoreCase(t.getIssueType()))
                .collect(java.util.stream.Collectors.toList());
            model.addAttribute("boardTitle", "Support Scrum Board");
        } else {
            allTickets = allTickets.stream()
                .filter(t -> !"IT_SUPPORT".equalsIgnoreCase(t.getIssueType()))
                .collect(java.util.stream.Collectors.toList());
            model.addAttribute("boardTitle", "QA Kanban Board");
        }

        // Group by status
        List<Ticket> openTickets = allTickets.stream().filter(t -> "OPEN".equalsIgnoreCase(t.getStatus())).collect(java.util.stream.Collectors.toList());
        List<Ticket> inProgressTickets = allTickets.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus()) || "ASSIGNED".equalsIgnoreCase(t.getStatus())).collect(java.util.stream.Collectors.toList());
        List<Ticket> resolvedTickets = allTickets.stream().filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus())).collect(java.util.stream.Collectors.toList());
        List<Ticket> onHoldTickets = allTickets.stream().filter(t -> "ON_HOLD".equalsIgnoreCase(t.getStatus())).collect(java.util.stream.Collectors.toList());

        System.out.println("DEBUG: Tickets found - Open: " + openTickets.size() + ", InProgress: " + inProgressTickets.size() + ", Resolved: " + resolvedTickets.size() + ", OnHold: " + onHoldTickets.size());

        model.addAttribute("openTickets", openTickets);
        model.addAttribute("inProgressTickets", inProgressTickets);
        model.addAttribute("resolvedTickets", resolvedTickets);
        model.addAttribute("onHoldTickets", onHoldTickets);
        model.addAttribute("issueType", issueType);

        return "scrum-board";
    }

    @PostMapping("/update-status-hx")
    @ResponseBody
    public String updateTicketStatusHx(
            @RequestParam Long ticketId,
            @RequestParam String status,
            HttpSession session) {
        
        Optional<Ticket> ticketOpt = ticketService.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            ticket.setStatus(status.toUpperCase());
            ticketService.save(ticket);
            return "OK";
        }
        return "Error";
    }

    
    // My Tickets – shows only tickets assigned to the currently logged-in user
    @GetMapping("/my-tickets")
    public String myTickets(
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model) {

        String userId      = (String) session.getAttribute("userId");
        String username    = (String) session.getAttribute("username");
        String companyCode = (String) session.getAttribute("companyCode");
        String role        = (String) session.getAttribute("role");

        if (userId == null) {
            return "redirect:/loginform";
        }

        List<Ticket> tickets = ticketService.getMyAssignedTicketsByCompany(userId, companyCode);

        // Apply optional status filter
        if (status != null && !status.isEmpty()) {
            tickets = tickets.stream()
                .filter(t -> status.equalsIgnoreCase(t.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        }

        // Stats by status
        long openCount       = tickets.stream().filter(t -> "OPEN".equalsIgnoreCase(t.getStatus())).count();
        long assignedCount   = tickets.stream().filter(t -> "ASSIGNED".equalsIgnoreCase(t.getStatus())).count();
        long inProgressCount = tickets.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
        long resolvedCount   = tickets.stream().filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus())).count();
        long closedCount     = tickets.stream().filter(t -> "CLOSED".equalsIgnoreCase(t.getStatus())).count();

        model.addAttribute("tickets",      tickets);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("isAdmin",      "ADMIN".equals(role));
        model.addAttribute("username",     username);
        model.addAttribute("openCount",       openCount);
        model.addAttribute("assignedCount",   assignedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("resolvedCount",   resolvedCount);
        model.addAttribute("closedCount",     closedCount);
        model.addAttribute("totalCount",      tickets.size());

        Map<String, String> departmentMap = new java.util.HashMap<>();
        List<com.example.demo.entity.Department> allDeptsForMap = departmentService.getAll();
        if (allDeptsForMap != null) {
            for (com.example.demo.entity.Department d : allDeptsForMap) {
                if (d.getDepartmentCode() != null) {
                    departmentMap.put(d.getDepartmentCode(), d.getDepartmentName());
                    if (d.getDepartmentName() != null) {
                        departmentMap.put(d.getDepartmentName(), d.getDepartmentName());
                    }
                }
            }
        }
        model.addAttribute("departmentMap", departmentMap);

        return "my-tickets";
    }
    
    // My Tickets – IT Support only
    @GetMapping("/my-tickets/it-support")
    public String myTicketsItSupport(@RequestParam(required = false) String status,
                                      HttpSession session,
                                      Model model) {
        String userId      = (String) session.getAttribute("userId");
        String username    = (String) session.getAttribute("username");
        String companyCode = (String) session.getAttribute("companyCode");
        String role        = (String) session.getAttribute("role");
        if (userId == null) {
            return "redirect:/loginform";
        }
        List<Ticket> tickets = ticketService.getMyAssignedTicketsByCompany(userId, companyCode);
        // Filter by IT_SUPPORT issue type
        tickets = tickets.stream()
                .filter(t -> "IT_SUPPORT".equalsIgnoreCase(t.getIssueType()))
                .collect(java.util.stream.Collectors.toList());
        // Apply optional status filter
        if (status != null && !status.isEmpty()) {
            tickets = tickets.stream()
                    .filter(t -> status.equalsIgnoreCase(t.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }
        // Stats
        long openCount       = tickets.stream().filter(t -> "OPEN".equalsIgnoreCase(t.getStatus())).count();
        long assignedCount   = tickets.stream().filter(t -> "ASSIGNED".equalsIgnoreCase(t.getStatus())).count();
        long inProgressCount = tickets.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
        long resolvedCount   = tickets.stream().filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus())).count();
        long closedCount     = tickets.stream().filter(t -> "CLOSED".equalsIgnoreCase(t.getStatus())).count();
        model.addAttribute("tickets",      tickets);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("isAdmin",      "ADMIN".equals(role));
        model.addAttribute("username",     username);
        model.addAttribute("openCount",       openCount);
        model.addAttribute("assignedCount",   assignedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("resolvedCount",   resolvedCount);
        model.addAttribute("closedCount",     closedCount);
        model.addAttribute("totalCount",      tickets.size());
        return "my-tickets-it-support";
    }

    // IT Tickets Opened by Me
    @GetMapping("/my-opened-tickets/it-support")
    public String myOpenedTicketsItSupport(@RequestParam(required = false) String status,
                                            HttpSession session,
                                            Model model) {
        String userId      = (String) session.getAttribute("userId");
        String username    = (String) session.getAttribute("username");
        String companyCode = (String) session.getAttribute("companyCode");
        String role        = (String) session.getAttribute("role");
        if (userId == null) {
            return "redirect:/loginform";
        }
        
        List<Ticket> tickets;
        if (companyCode != null && !companyCode.isEmpty() && !"ADMIN".equals(role)) {
            tickets = ticketService.getByCompanyCode(companyCode);
        } else {
            tickets = ticketService.getAllTickets();
        }
        
        // Filter by IT_SUPPORT issue type AND createdBy / userCode matching the current username/userId
        tickets = tickets.stream()
                .filter(t -> "IT_SUPPORT".equalsIgnoreCase(t.getIssueType()) || "IT Support".equalsIgnoreCase(t.getIssueType()))
                .filter(t -> (t.getUserCode() != null && t.getUserCode().equalsIgnoreCase(username)) || 
                             (t.getCreatedBy() != null && t.getCreatedBy().equalsIgnoreCase(username)))
                .collect(java.util.stream.Collectors.toList());
                
        if (status != null && !status.isEmpty()) {
            tickets = tickets.stream()
                    .filter(t -> status.equalsIgnoreCase(t.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        long openCount       = tickets.stream().filter(t -> "OPEN".equalsIgnoreCase(t.getStatus())).count();
        long assignedCount   = tickets.stream().filter(t -> "ASSIGNED".equalsIgnoreCase(t.getStatus())).count();
        long inProgressCount = tickets.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
        long resolvedCount   = tickets.stream().filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus())).count();
        long closedCount     = tickets.stream().filter(t -> "CLOSED".equalsIgnoreCase(t.getStatus())).count();
        
        java.util.Map<String, String> assigneeRoles = new java.util.HashMap<>();
        for (Ticket t : tickets) {
            String assigneeCode = t.getAssigneeUserCode();
            if (assigneeCode != null && !assigneeCode.trim().isEmpty() && !assigneeRoles.containsKey(assigneeCode)) {
                UserAccount user = userAccountService.findByUserId(assigneeCode);
                if (user != null && user.getUserRole() != null) {
                    assigneeRoles.put(assigneeCode, formatRole(user.getUserRole()));
                }
            }
        }
        
        model.addAttribute("tickets",      tickets);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("isAdmin",      "ADMIN".equals(role));
        model.addAttribute("username",     username);
        model.addAttribute("openCount",       openCount);
        model.addAttribute("assignedCount",   assignedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("resolvedCount",   resolvedCount);
        model.addAttribute("closedCount",     closedCount);
        model.addAttribute("totalCount",      tickets.size());
        model.addAttribute("assigneeRoles",   assigneeRoles);
        
        return "my-opened-tickets-it-support";
    }

    // QA Tickets Opened by Me
    @GetMapping("/my-opened-tickets/qa")
    public String myOpenedTicketsQa(@RequestParam(required = false) String status,
                                    HttpSession session,
                                    Model model) {
        String userId      = (String) session.getAttribute("userId");
        String username    = (String) session.getAttribute("username");
        String companyCode = (String) session.getAttribute("companyCode");
        String role        = (String) session.getAttribute("role");
        if (userId == null) {
            return "redirect:/loginform";
        }
        
        List<Ticket> tickets;
        if (companyCode != null && !companyCode.isEmpty() && !"ADMIN".equals(role)) {
            tickets = ticketService.getByCompanyCode(companyCode);
        } else {
            tickets = ticketService.getAllTickets();
        }
        
        // Filter by non-IT_SUPPORT issue type AND createdBy / userCode matching the current username/userId
        tickets = tickets.stream()
                .filter(t -> t.getIssueType() == null || (!"IT_SUPPORT".equalsIgnoreCase(t.getIssueType()) && !"IT Support".equalsIgnoreCase(t.getIssueType())))
                .filter(t -> (t.getUserCode() != null && t.getUserCode().equalsIgnoreCase(username)) || 
                             (t.getCreatedBy() != null && t.getCreatedBy().equalsIgnoreCase(username)))
                .collect(java.util.stream.Collectors.toList());
                
        if (status != null && !status.isEmpty()) {
            tickets = tickets.stream()
                    .filter(t -> status.equalsIgnoreCase(t.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        long openCount       = tickets.stream().filter(t -> "OPEN".equalsIgnoreCase(t.getStatus())).count();
        long assignedCount   = tickets.stream().filter(t -> "ASSIGNED".equalsIgnoreCase(t.getStatus())).count();
        long inProgressCount = tickets.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
        long resolvedCount   = tickets.stream().filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus())).count();
        long closedCount     = tickets.stream().filter(t -> "CLOSED".equalsIgnoreCase(t.getStatus())).count();
        
        java.util.Map<String, String> assigneeRoles = new java.util.HashMap<>();
        for (Ticket t : tickets) {
            String assigneeCode = t.getAssigneeUserCode();
            if (assigneeCode != null && !assigneeCode.trim().isEmpty() && !assigneeRoles.containsKey(assigneeCode)) {
                UserAccount user = userAccountService.findByUserId(assigneeCode);
                if (user != null && user.getUserRole() != null) {
                    assigneeRoles.put(assigneeCode, formatRole(user.getUserRole()));
                }
            }
        }
        
        model.addAttribute("tickets",      tickets);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("isAdmin",      "ADMIN".equals(role));
        model.addAttribute("username",     username);
        model.addAttribute("openCount",       openCount);
        model.addAttribute("assignedCount",   assignedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("resolvedCount",   resolvedCount);
        model.addAttribute("closedCount",     closedCount);
        model.addAttribute("totalCount",      tickets.size());
        model.addAttribute("assigneeRoles",   assigneeRoles);
        
        return "my-opened-tickets-qa";
    }

    // My Tickets – QA only
    @GetMapping("/my-tickets/qa")
    public String myTicketsQa(@RequestParam(required = false) String status,
                              HttpSession session,
                              Model model) {
        String userId      = (String) session.getAttribute("userId");
        String username    = (String) session.getAttribute("username");
        String companyCode = (String) session.getAttribute("companyCode");
        String role        = (String) session.getAttribute("role");
        if (userId == null) {
            return "redirect:/loginform";
        }
        List<Ticket> tickets = ticketService.getMyAssignedTicketsByCompany(userId, companyCode);
        // Filter by non‑IT_SUPPORT (QA) issue type
        tickets = tickets.stream()
                .filter(t -> !"IT_SUPPORT".equalsIgnoreCase(t.getIssueType()))
                .collect(java.util.stream.Collectors.toList());
        // Apply optional status filter
        if (status != null && !status.isEmpty()) {
            tickets = tickets.stream()
                    .filter(t -> status.equalsIgnoreCase(t.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }
        // Stats
        long openCount       = tickets.stream().filter(t -> "OPEN".equalsIgnoreCase(t.getStatus())).count();
        long assignedCount   = tickets.stream().filter(t -> "ASSIGNED".equalsIgnoreCase(t.getStatus())).count();
        long inProgressCount = tickets.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
        long resolvedCount   = tickets.stream().filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus())).count();
        long closedCount     = tickets.stream().filter(t -> "CLOSED".equalsIgnoreCase(t.getStatus())).count();
        model.addAttribute("tickets",      tickets);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("isAdmin",      "ADMIN".equals(role));
        model.addAttribute("username",     username);
        model.addAttribute("openCount",       openCount);
        model.addAttribute("assignedCount",   assignedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("resolvedCount",   resolvedCount);
        model.addAttribute("closedCount",     closedCount);
        model.addAttribute("totalCount",      tickets.size());
        model.addAttribute("listTitle", "My QA Tickets");
        return "my-tickets-qa";
    }

    @RequestMapping(value = "/view/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public String viewTicket(@PathVariable Long id, HttpSession session, Model model) {
        try {
            Ticket ticket = ticketService.getById(id);
            List<TicketComment> comments = ticketService.getComments(id);
            List<TicketHistory> history = ticketService.getHistory(id);
            List<TicketTask> tasks = ticketTaskRepository.findByTicketId(id);
            
            String role = (String) session.getAttribute("role");
            String companyCode = (String) session.getAttribute("companyCode");
            String userId = (String) session.getAttribute("userId");
            
            // Get users from the same company for assignment dropdown
            List<UserAccount> companyUsers = null;
            String upperRole = (role != null) ? role.toUpperCase() : "GUEST";
            
            if (companyCode != null && !companyCode.isEmpty() && !"-".equals(companyCode)) {
                companyUsers = userAccountService.getUsersByCompanyCode(companyCode);
            } else if (ticket.getCompanyCode() != null && !ticket.getCompanyCode().isEmpty() && !"-".equals(ticket.getCompanyCode())) {
                companyUsers = userAccountService.getUsersByCompanyCode(ticket.getCompanyCode());
            }
            
            // If still empty and user is ADMIN, provide all users in the system
            if ((companyUsers == null || companyUsers.isEmpty()) && "ADMIN".equals(upperRole)) {
                companyUsers = userAccountService.getAllUsers();
            }
            
            // Filter tasks based on role
            if (!"ADMIN".equals(upperRole) && !"BUSINESS_MANAGER".equals(upperRole)) {
                tasks = tasks.stream()
                        .filter(t -> userId != null && userId.equalsIgnoreCase(t.getPerformedBy()))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            model.addAttribute("ticket", ticket);
            model.addAttribute("comments", comments);
            model.addAttribute("history", history);
            model.addAttribute("tasks", tasks);
            model.addAttribute("isAdmin", "ADMIN".equals(role));
            model.addAttribute("companyUsers", companyUsers);
            model.addAttribute("taskTypes", taskTypeRepository.findAll());
            model.addAttribute("canEdit", canEditTicket(ticket, session));
            
            // Choose view template based on issue type
            if ("IT_SUPPORT".equalsIgnoreCase(ticket.getIssueType())) {
                return "ticket-view-support";
            }
            return "ticket-view-qa";
        } catch (Exception e) {
            model.addAttribute("error", "Ticket not found: " + e.getMessage());
            return "redirect:/api/tickets/list";
        }
    }
    
    // Edit ticket form
    @GetMapping("/edit/{id}")
    public String editTicketForm(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            Ticket ticket = ticketService.getById(id);
            
            if (!canEditTicket(ticket, session)) {
                redirectAttributes.addFlashAttribute("error", "Only person who created or Manager or assignee can edit ticket");
                return "redirect:/api/tickets/view/" + id;
            }
            if ("CLOSED".equalsIgnoreCase(ticket.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Ticket is closed. Please reopen it first to edit.");
                return "redirect:/api/tickets/view/" + id;
            }
            
            // Get company users for assignment dropdown
            String companyCode = ticket.getCompanyCode();
            if (companyCode == null || companyCode.isEmpty()) {
                companyCode = (String) session.getAttribute("companyCode");
            }
            if (companyCode != null && !companyCode.isEmpty()) {
                List<UserAccount> companyUsers = userAccountService.getUsersByCompanyCode(companyCode);
                model.addAttribute("companyUsers", companyUsers);
                
                // Get applications for this company
                List<ProjectRef> projects = projectRepository.findByCompanyCode(companyCode);
                if (!projects.isEmpty()) {
                    List<ApplicationRef> applications = new java.util.ArrayList<>();
                    for (ProjectRef project : projects) {
                        applications.addAll(applicationRepository.findByProjectCode(project.getProjectCode()));
                    }
                    model.addAttribute("applications", applications);
                }
                
                // Get all test cases for this company
                List<TestCaseHeader> testCases = testCaseHeaderRepository.findByCompanyCode(companyCode);
                model.addAttribute("testCases", testCases);
            }
            
            // Get all modules
            List<ModuleRef> modules = moduleRepository.findAll();
            model.addAttribute("modules", modules);
            
            model.addAttribute("ticket", ticket);
            model.addAttribute("isEdit", true);
            model.addAttribute("transaction", null);
            if ("IT_SUPPORT".equalsIgnoreCase(ticket.getIssueType()) || "IT Support".equalsIgnoreCase(ticket.getIssueType())) {
                model.addAttribute("supportIssues", supportIssueTypeRepository.findAll());
                return "ticket-form-support";
            }
            return "ticket-form-qa";
        } catch (Exception e) {
            model.addAttribute("error", "Ticket not found");
            return "redirect:/api/tickets/list";
        }
    }
    
    // Update ticket
    @PostMapping("/update/{id}")
    public String updateTicket(
            @PathVariable Long id,
            @ModelAttribute("ticket") Ticket ticket,
            @RequestParam(value = "attachmentFiles", required = false) MultipartFile[] files,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            Ticket existingTicket = ticketService.getById(id);
            if (!canEditTicket(existingTicket, session)) {
                redirectAttributes.addFlashAttribute("error", "Only person who created or Manager or assignee can edit ticket");
                return "redirect:/api/tickets/view/" + id;
            }
            String username = (String) session.getAttribute("userId");
            if (username == null) username = (String) session.getAttribute("username");
            if (username == null) username = "System";
            
            String ticketNoPrefix = (ticket.getTicketNo() != null && !ticket.getTicketNo().trim().isEmpty())
                    ? ticket.getTicketNo()
                    : (existingTicket != null && existingTicket.getTicketNo() != null ? existingTicket.getTicketNo() : "TKT-" + id);

            // Handle file uploads
            if (files != null && files.length > 0) {
                StringBuilder paths = new StringBuilder();
                String uploadDir = Paths.get("uploads", "tickets").toAbsolutePath().toString();
                new File(uploadDir).mkdirs();
                
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String fileName = buildTicketAttachmentFilename(ticketNoPrefix, file.getOriginalFilename(), uploadDir);
                        File destination = new File(uploadDir, fileName);
                        file.transferTo(destination);
                        
                        if (paths.length() > 0) paths.append(",");
                        paths.append(fileName);
                    }
                }
                
                if (paths.length() > 0) {
                    ticket.setAttachments(paths.toString());
                }
            }
            
            ticketService.updateTicket(id, ticket, username);
            redirectAttributes.addFlashAttribute("success", "Ticket updated successfully!");
            return "redirect:/api/tickets/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update ticket: " + e.getMessage());
            return "redirect:/api/tickets/edit/" + id;
        }
    }

    // AJAX endpoint to upload attachments directly
    @PostMapping("/ajax/upload-attachments/{id}")
    @ResponseBody
    public java.util.Map<String, Object> ajaxUploadAttachments(
            @PathVariable Long id,
            @RequestParam("attachmentFiles") MultipartFile[] files,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            Ticket ticket = ticketService.getById(id);
            if (!canEditTicket(ticket, session)) {
                response.put("success", false);
                response.put("message", "Only person who created or Manager or assignee can edit ticket");
                return response;
            }
            
            String username = (String) session.getAttribute("userId");
            if (username == null) username = (String) session.getAttribute("username");
            if (username == null) username = "System";
            
            Ticket existingTicket = ticketService.getById(id);
            String ticketNoPrefix = existingTicket != null && existingTicket.getTicketNo() != null
                    ? existingTicket.getTicketNo()
                    : "TKT-" + id;

            if (files != null && files.length > 0) {
                StringBuilder paths = new StringBuilder();
                String uploadDir = Paths.get("uploads", "tickets").toAbsolutePath().toString();
                new File(uploadDir).mkdirs();
                
                java.util.List<String> fileNames = new java.util.ArrayList<>();
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String fileName = buildTicketAttachmentFilename(ticketNoPrefix, file.getOriginalFilename(), uploadDir);
                        File destination = new File(uploadDir, fileName);
                        file.transferTo(destination);
                        
                        if (paths.length() > 0) paths.append(",");
                        paths.append(fileName);
                        fileNames.add(fileName);
                    }
                }
                
                if (paths.length() > 0) {
                    ticketService.addAttachments(id, paths.toString(), username);
                    response.put("success", true);
                    response.put("fileNames", fileNames);
                    response.put("message", "Attachments uploaded successfully");
                    return response;
                }
            }
            response.put("success", false);
            response.put("message", "No files selected");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to upload attachments: " + e.getMessage());
        }
        return response;
    }

    // AJAX endpoint to delete an attachment
    @PostMapping("/ajax/delete-attachment/{id}")
    @ResponseBody
    public java.util.Map<String, Object> ajaxDeleteAttachment(
            @PathVariable Long id,
            @RequestParam("fileName") String fileName,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            Ticket ticket = ticketService.getById(id);
            if (!canEditTicket(ticket, session)) {
                response.put("success", false);
                response.put("message", "Permission denied to edit attachments on this ticket");
                return response;
            }

            String username = (String) session.getAttribute("userId");
            if (username == null) username = (String) session.getAttribute("username");
            if (username == null) username = "System";

            ticketService.deleteAttachment(id, fileName, username);
            response.put("success", true);
            response.put("fileName", fileName);
            response.put("message", "Attachment deleted successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete attachment: " + e.getMessage());
        }
        return response;
    }

    // AJAX endpoint to replace an attachment
    @PostMapping("/ajax/replace-attachment/{id}")
    @ResponseBody
    public java.util.Map<String, Object> ajaxReplaceAttachment(
            @PathVariable Long id,
            @RequestParam("oldFileName") String oldFileName,
            @RequestParam("replacementFile") MultipartFile file,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            Ticket ticket = ticketService.getById(id);
            if (!canEditTicket(ticket, session)) {
                response.put("success", false);
                response.put("message", "Permission denied to edit attachments on this ticket");
                return response;
            }

            if (file == null || file.isEmpty()) {
                response.put("success", false);
                response.put("message", "No replacement file provided");
                return response;
            }

            String username = (String) session.getAttribute("userId");
            if (username == null) username = (String) session.getAttribute("username");
            if (username == null) username = "System";

            String ticketNoPrefix = ticket != null && ticket.getTicketNo() != null
                    ? ticket.getTicketNo()
                    : "TKT-" + id;

            String uploadDir = Paths.get("uploads", "tickets").toAbsolutePath().toString();
            new File(uploadDir).mkdirs();

            String newFileName = buildTicketAttachmentFilename(ticketNoPrefix, file.getOriginalFilename(), uploadDir);
            File destination = new File(uploadDir, newFileName);
            file.transferTo(destination);

            ticketService.replaceAttachment(id, oldFileName, newFileName, username);
            response.put("success", true);
            response.put("oldFileName", oldFileName);
            response.put("newFileName", newFileName);
            response.put("message", "Attachment replaced successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to replace attachment: " + e.getMessage());
        }
        return response;
    }
    
    // Change status
    @PostMapping("/status/{id}")
    public String changeStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String notes,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (notes == null || notes.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Notes are mandatory when changing ticket status");
                return "redirect:/api/tickets/view/" + id;
            }
            Ticket ticket = ticketService.getById(id);
            if ("CLOSED".equalsIgnoreCase(status) && !canEditTicket(ticket, session)) {
                redirectAttributes.addFlashAttribute("error", "Only the person who created the ticket, managers, or assignees can close the ticket");
                return "redirect:/api/tickets/view/" + id;
            }

            String username = (String) session.getAttribute("username");
            if (username == null) username = "System";
            
            ticketService.changeStatus(id, status, username, notes);
            redirectAttributes.addFlashAttribute("success", "Status changed to " + status);
            return "redirect:/api/tickets/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to change status: " + e.getMessage());
            return "redirect:/api/tickets/view/" + (id != null ? id : "");
        }
    }
    
    // Assign ticket
    @PostMapping("/assign/{id}")
    public String assignTicket(
            @PathVariable Long id,
            @RequestParam String assignedTo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            Ticket ticket = ticketService.getById(id);
            if (!canEditTicket(ticket, session)) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to assign this ticket");
                return "redirect:/api/tickets/view/" + id;
            }
            
            String userId = (String) session.getAttribute("userId");
            String username = (String) session.getAttribute("username");
            if (userId == null) userId = "System";
            if (username == null) username = "System";
            
            System.out.println("Assigning ticket " + id + " to: " + assignedTo);
            System.out.println("Assigned by userId: " + userId + ", username: " + username);
            
            ticketService.assignTicket(id, assignedTo.trim(), userId, username);
            redirectAttributes.addFlashAttribute("success", "Ticket assigned to " + assignedTo);
            return "redirect:/api/tickets/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to assign ticket: " + e.getMessage());
            return "redirect:/api/tickets/view/" + id;
        }
    }
    
    // Add comment
    @PostMapping("/comment/{id}")
    public String addComment(
            @PathVariable Long id,
            @RequestParam String comment,
            @RequestParam(value = "commentAttachment", required = false) MultipartFile file,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            Ticket ticket = ticketService.getById(id);
            if (!canEditTicket(ticket, session)) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to add comments to this ticket");
                return "redirect:/api/tickets/view/" + id;
            }
            
            String username = (String) session.getAttribute("username");
            if (username == null) username = "System";
            
            Ticket existingTicket = ticketService.getById(id);
            String ticketNoPrefix = existingTicket != null && existingTicket.getTicketNo() != null
                    ? existingTicket.getTicketNo()
                    : "TKT-" + id;

            String attachmentPath = null;
            if (file != null && !file.isEmpty()) {
                String uploadDir = Paths.get("uploads", "tickets", "comments").toAbsolutePath().toString();
                new File(uploadDir).mkdirs();
                String fileName = buildTicketAttachmentFilename(ticketNoPrefix, file.getOriginalFilename(), uploadDir);
                File destination = new File(uploadDir, fileName);
                file.transferTo(destination);
                attachmentPath = fileName;
            }
            
            ticketService.addComment(id, comment, username, attachmentPath);
            redirectAttributes.addFlashAttribute("success", "Comment added successfully");
            return "redirect:/api/tickets/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add comment: " + e.getMessage());
            return "redirect:/api/tickets/view/" + id;
        }
    }
    
    // Close ticket quickly
    @GetMapping("/close/{id}")
    public String closeTicket(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            Ticket ticket = ticketService.getById(id);
            if (!canEditTicket(ticket, session)) {
                redirectAttributes.addFlashAttribute("error", "Only the person who created the ticket, managers, or assignees can close the ticket");
                return "redirect:/api/tickets/view/" + id;
            }

            // Validate Billable Hours
            List<TicketTask> tasks = ticketTaskRepository.findByTicketId(id);
            boolean hasInvalidTask = tasks.stream().anyMatch(t -> t.getBillableHours() == null);
            if (hasInvalidTask) {
                redirectAttributes.addFlashAttribute("error", "Billable Hours needs to be filled before closing a task or ticket, whichever is applicable");
                return "redirect:/api/tickets/view/" + id;
            }

            String username = (String) session.getAttribute("username");
            if (username == null) username = "System";
            
            ticketService.changeStatus(id, "CLOSED", username, "Ticket closed");
            redirectAttributes.addFlashAttribute("success", "Ticket closed successfully!");
            return "redirect:/api/tickets/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to close ticket: " + e.getMessage());
            return "redirect:/api/tickets/view/" + (id != null ? id : "");
        }
    }
    
    // Soft delete ticket (moves to deleted tickets)
    @GetMapping("/delete/{id}")
    public String deleteTicket(
            @PathVariable Long id, 
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String role = (String) session.getAttribute("role");
        Ticket ticket = null;
        try {
            ticket = ticketService.getById(id);
        } catch (Exception e) {}
        
        boolean canDelete = false;
        if (ticket != null && canEditTicket(ticket, session)) {
            String issueType = ticket.getIssueType();
            boolean isItSupport = "IT_SUPPORT".equalsIgnoreCase(issueType) || "IT Support".equalsIgnoreCase(issueType);
            if (isItSupport) {
                canDelete = "CLOSED".equalsIgnoreCase(ticket.getStatus()) && ("ADMIN".equals(role) || "BUSINESS_MANAGER".equals(role));
            } else {
                canDelete = "CLOSED".equalsIgnoreCase(ticket.getStatus()) && ("ADMIN".equals(role) || "BUSINESS_MANAGER".equals(role));
            }
        }

        if (!canDelete) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to delete tickets");
            return "redirect:/api/tickets/list";
        }
        
        try {
            String username = (String) session.getAttribute("username");
            if (username == null) username = "System";
            
            ticketService.deleteTicket(id, username);
            redirectAttributes.addFlashAttribute("success", "Ticket moved to Deleted Tickets!");
            if (ticket != null && "IT_SUPPORT".equalsIgnoreCase(ticket.getIssueType())) {
                return "redirect:/api/tickets/list?issueType=IT_SUPPORT";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete ticket: " + e.getMessage());
        }
        return "redirect:/api/tickets/list";
    }
    
    // List deleted tickets
    @GetMapping("/deleted")
    public String listDeletedTickets(
            @RequestParam(required = false) String issueType,
            HttpSession session, 
            Model model) {
        
        String companyCode = (String) session.getAttribute("companyCode");
        String role = (String) session.getAttribute("role");
        
        if (!"BUSINESS_MANAGER".equals(role) && !"ADMIN".equals(role)) {
            return "redirect:/api/tickets/list";
        }
        
        List<Ticket> deletedTickets;
        
        if (companyCode != null && !companyCode.isEmpty()) {
            deletedTickets = ticketService.getDeletedTicketsByCompanyCode(companyCode);
        } else {
            deletedTickets = ticketService.getDeletedTickets();
        }

        // Apply issueType filtering for deleted tickets
        if ("IT_SUPPORT".equalsIgnoreCase(issueType)) {
            deletedTickets = deletedTickets.stream()
                .filter(t -> "IT_SUPPORT".equalsIgnoreCase(t.getIssueType()))
                .collect(java.util.stream.Collectors.toList());
            model.addAttribute("listTitle", "Deleted Support Tickets");
        } else {
            deletedTickets = deletedTickets.stream()
                .filter(t -> !"IT_SUPPORT".equalsIgnoreCase(t.getIssueType()))
                .collect(java.util.stream.Collectors.toList());
            model.addAttribute("listTitle", "Deleted Standard Tickets");
        }
        
        model.addAttribute("tickets", deletedTickets);
        model.addAttribute("isAdmin", "ADMIN".equals(role));
        model.addAttribute("isDeletedView", true);
        model.addAttribute("selectedIssueType", issueType);
        
        return "ticket-deleted-list";
    }
    
    // Restore deleted ticket
    @GetMapping("/restore/{id}")
    public String restoreTicket(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String role = (String) session.getAttribute("role");
        if (!"BUSINESS_MANAGER".equals(role) && !"ADMIN".equals(role)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to restore tickets");
            return "redirect:/api/tickets/list";
        }
        
        try {
            String username = (String) session.getAttribute("username");
            if (username == null) username = "System";
            
            Ticket ticket = ticketService.getById(id);
            ticketService.restoreTicket(id, username);
            redirectAttributes.addFlashAttribute("success", "Ticket restored successfully!");
            if (ticket != null && "IT_SUPPORT".equalsIgnoreCase(ticket.getIssueType())) {
                return "redirect:/api/tickets/deleted?issueType=IT_SUPPORT";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to restore ticket: " + e.getMessage());
        }
        return "redirect:/api/tickets/deleted";
    }
    
    // Permanently delete ticket
    @GetMapping("/permanent-delete/{id}")
    public String permanentlyDeleteTicket(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String role = (String) session.getAttribute("role");
        if (!"BUSINESS_MANAGER".equals(role) && !"ADMIN".equals(role)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to permanently delete tickets");
            return "redirect:/api/tickets/list";
        }
        
        try {
            ticketService.permanentlyDeleteTicket(id);
            redirectAttributes.addFlashAttribute("success", "Ticket permanently deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to permanently delete ticket: " + e.getMessage());
        }
        return "redirect:/api/tickets/deleted";
    }
    
    // ============= AJAX ENDPOINTS (No page reload) =============
    
    @PostMapping("/ajax/status/{id}")
    @ResponseBody
    public java.util.Map<String, Object> ajaxChangeStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String notes,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            if (notes == null || notes.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Notes are mandatory when changing ticket status");
                return response;
            }
            Ticket ticket = ticketService.getById(id);
            if ("CLOSED".equalsIgnoreCase(status) && !canEditTicket(ticket, session)) {
                response.put("success", false);
                response.put("message", "Only the person who created the ticket, managers, or assignees can close the ticket");
                return response;
            }

            String username = (String) session.getAttribute("username");
            if (username == null) username = "System";
            
            ticketService.changeStatus(id, status, username, notes);
            response.put("success", true);
            response.put("message", "Status changed to " + status);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/ajax/assign/{id}")
    @ResponseBody
    public java.util.Map<String, Object> ajaxAssignTicket(
            @PathVariable Long id,
            @RequestParam String assignedTo,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            Ticket ticket = ticketService.getById(id);
            if (!canEditTicket(ticket, session)) {
                response.put("success", false);
                response.put("message", "You don't have permission to assign this ticket");
                return response;
            }
            
            String userId = (String) session.getAttribute("userId");
            String username = (String) session.getAttribute("username");
            if (userId == null) userId = "System";
            if (username == null) username = "System";
            
            System.out.println("AJAX: Assigning ticket " + id + " to: " + assignedTo);
            System.out.println("AJAX: Assigned by userId: " + userId + ", username: " + username);
            
            ticketService.assignTicket(id, assignedTo.trim(), userId, username);
            response.put("success", true);
            response.put("message", "Ticket assigned to " + assignedTo);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/ajax/comment/{id}")
    @ResponseBody
    public java.util.Map<String, Object> ajaxAddComment(
            @PathVariable Long id,
            @RequestParam String comment,
            @RequestParam(value = "commentAttachment", required = false) MultipartFile file,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            Ticket ticket = ticketService.getById(id);
            if (!canEditTicket(ticket, session)) {
                response.put("success", false);
                response.put("message", "You don't have permission to add comments to this ticket");
                return response;
            }
            
            String username = (String) session.getAttribute("username");
            if (username == null) username = "System";
            
            Ticket existingTicket = ticketService.getById(id);
            String ticketNoPrefix = existingTicket != null && existingTicket.getTicketNo() != null
                    ? existingTicket.getTicketNo()
                    : "TKT-" + id;

            String attachmentPath = null;
            if (file != null && !file.isEmpty()) {
                String uploadDir = Paths.get("uploads", "tickets", "comments").toAbsolutePath().toString();
                new File(uploadDir).mkdirs();
                String fileName = buildTicketAttachmentFilename(ticketNoPrefix, file.getOriginalFilename(), uploadDir);
                File destination = new File(uploadDir, fileName);
                file.transferTo(destination);
                attachmentPath = fileName;
            }
            
            ticketService.addComment(id, comment, username, attachmentPath);
            response.put("success", true);
            response.put("message", "Comment added successfully");
            if (attachmentPath != null) {
                response.put("attachmentPath", attachmentPath);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/ajax/close/{id}")
    @ResponseBody
    public java.util.Map<String, Object> ajaxCloseTicket(
            @PathVariable Long id,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            Ticket ticket = ticketService.getById(id);
            if (!canEditTicket(ticket, session)) {
                response.put("success", false);
                response.put("message", "Only the person who created the ticket, managers, or assignees can close the ticket");
                return response;
            }

            // Validate Billable Hours
            List<TicketTask> tasks = ticketTaskRepository.findByTicketId(id);
            boolean hasInvalidTask = tasks.stream().anyMatch(t -> t.getBillableHours() == null);
            if (hasInvalidTask) {
                response.put("success", false);
                response.put("message", "Billable Hours needs to be filled before closing a task or ticket, whichever is applicable");
                return response;
            }

            String username = (String) session.getAttribute("username");
            if (username == null) username = "System";
            
            ticketService.changeStatus(id, "CLOSED", username, "Ticket closed");
            response.put("success", true);
            response.put("message", "Ticket closed successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/ajax/delete/{id}")
    @ResponseBody
    public java.util.Map<String, Object> ajaxDeleteTicket(
            @PathVariable Long id,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        String role = (String) session.getAttribute("role");
        Ticket ticket = null;
        try {
            ticket = ticketService.getById(id);
        } catch (Exception e) {}
        
        boolean canDelete = false;
        if (ticket != null && canEditTicket(ticket, session)) {
            String issueType = ticket.getIssueType();
            boolean isItSupport = "IT_SUPPORT".equalsIgnoreCase(issueType) || "IT Support".equalsIgnoreCase(issueType);
            if (isItSupport) {
                canDelete = "CLOSED".equalsIgnoreCase(ticket.getStatus()) && ("ADMIN".equals(role) || "BUSINESS_MANAGER".equals(role));
            } else {
                canDelete = "CLOSED".equalsIgnoreCase(ticket.getStatus()) && ("ADMIN".equals(role) || "BUSINESS_MANAGER".equals(role));
            }
        }

        if (!canDelete) {
            response.put("success", false);
            response.put("message", "You don't have permission to delete tickets");
            return response;
        }
        
        try {
            String username = (String) session.getAttribute("username");
            if (username == null) username = "System";
            
            ticketService.deleteTicket(id, username);
            response.put("success", true);
            response.put("message", "Ticket moved to Deleted Tickets");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed: " + e.getMessage());
        }
        return response;
    }
       @PostMapping("/ajax/restore/{id}")
    @ResponseBody
    public java.util.Map<String, Object> ajaxRestoreTicket(
            @PathVariable Long id,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        String role = (String) session.getAttribute("role");
        if (!"BUSINESS_MANAGER".equals(role) && !"ADMIN".equals(role)) {
            response.put("success", false);
            response.put("message", "You don't have permission to restore tickets");
            return response;
        }
        
        try {
            String username = (String) session.getAttribute("username");
            if (username == null) username = "System";
            
            ticketService.restoreTicket(id, username);
            response.put("success", true);
            response.put("message", "Ticket restored successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed: " + e.getMessage());
        }
        return response;
    }

    // ==================== TICKET TASKS ENDPOINTS ====================

    @PostMapping("/tasks/save")
    public String saveTask(
            @RequestParam Long ticketId,
            @ModelAttribute TicketTask task,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            if ("CLOSED".equals(task.getStatus())) {
                if (task.getBillableHours() == null) {
                    redirectAttributes.addFlashAttribute("error", "Billable Hours needs to be filled before closing a task");
                    return "redirect:/api/tickets/view/" + ticketId;
                }
                if (task.getCompletedDate() == null) {
                    redirectAttributes.addFlashAttribute("error", "Completed Date needs to be filled before closing a task");
                    return "redirect:/api/tickets/view/" + ticketId;
                }
            }

            Ticket ticket = ticketService.getById(ticketId);
            if (!canEditTicket(ticket, session)) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to add/edit tasks for this ticket");
                return "redirect:/api/tickets/view/" + ticketId;
            }
            task.setTicket(ticket);
            
            if (task.getId() == null) {
                // New task
                if (task.getStatus() == null) task.setStatus("OPEN");
                if (task.getStartDate() == null) task.setStartDate(java.time.LocalDate.now());
                task.setCompanyCode(ticket.getCompanyCode());
                
                String currentUsername = (String) session.getAttribute("username");
                if (currentUsername == null) currentUsername = (String) session.getAttribute("userId");
                if (currentUsername == null) currentUsername = "System";
                task.setLastModifiedBy(currentUsername);
                task.setLastModifiedAt(java.time.LocalDateTime.now());
            } else {
                // Update task
                TicketTask existingTask = ticketTaskRepository.findById(task.getId())
                        .orElseThrow(() -> new RuntimeException("Task not found"));
                        
                String oldStatus = existingTask.getStatus();
                
                task.setTicket(existingTask.getTicket());
                task.setStartDate(existingTask.getStartDate());
                task.setCompanyCode(existingTask.getCompanyCode() != null ? existingTask.getCompanyCode() : ticket.getCompanyCode());
                if (task.getStatus() == null) task.setStatus(existingTask.getStatus());
                
                // Notify if a CLOSED task was edited
                if ("CLOSED".equals(oldStatus)) {
                    String username = (String) session.getAttribute("username");
                    if (username == null) username = "System";
                    String userId = (String) session.getAttribute("userId");
                    if (userId == null) userId = "System";
                    
                    String notifyUser = ticket.getAssigneeUserCode();
                    if (notifyUser == null || notifyUser.isEmpty()) {
                        notifyUser = ticket.getUserCode(); // fallback to creator
                    }
                    if (notifyUser != null && !notifyUser.isEmpty()) {
                        notificationService.createTicketTaskEditNotification(
                            ticket, 
                            notifyUser, 
                            userId, 
                            username, 
                            task.getTaskType()
                        );
                    }
                }
                
                // Track modification history
                String currentUsername = (String) session.getAttribute("username");
                if (currentUsername == null) currentUsername = (String) session.getAttribute("userId");
                if (currentUsername == null) currentUsername = "System";
                task.setLastModifiedBy(currentUsername);
                task.setLastModifiedAt(java.time.LocalDateTime.now());
            }
            
            TicketTask savedTask = ticketTaskRepository.save(task);
            
            // Notify creator if the person updating/creating the task is not the creator
            try {
                String currentUsername = (String) session.getAttribute("username");
                if (currentUsername == null) currentUsername = (String) session.getAttribute("userId");
                if (currentUsername == null) currentUsername = "System";
                
                String creator = ticket.getUserCode() != null ? ticket.getUserCode().trim().toLowerCase() : null;
                if (creator != null && !creator.equalsIgnoreCase(currentUsername.trim().toLowerCase())) {
                    notificationService.createTicketUpdateNotification(
                        ticket,
                        creator,
                        currentUsername.trim().toLowerCase(),
                        currentUsername,
                        "Task '" + task.getTaskType() + "' was " + (task.getId() == null ? "added" : "updated")
                    );
                }
            } catch (Exception e) {
                System.err.println("Failed to send task update notification to creator: " + e.getMessage());
            }
            
            redirectAttributes.addFlashAttribute("success", "Task saved successfully!");
            return "redirect:/api/tickets/view/" + ticketId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to save task: " + e.getMessage());
            return "redirect:/api/tickets/view/" + ticketId;
        }
    }

    @PostMapping("/ajax/tasks/save")
    @ResponseBody
    public java.util.Map<String, Object> ajaxSaveTask(
            @RequestParam Long ticketId,
            @ModelAttribute TicketTask task,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        if ("CLOSED".equals(task.getStatus())) {
            if (task.getBillableHours() == null) {
                response.put("success", false);
                response.put("message", "Billable Hours needs to be filled before closing a task");
                return response;
            }
            if (task.getCompletedDate() == null) {
                response.put("success", false);
                response.put("message", "Completed Date needs to be filled before closing a task");
                return response;
            }
        }

        try {
            Ticket ticket = ticketService.getById(ticketId);
            if (!canEditTicket(ticket, session)) {
                response.put("success", false);
                response.put("message", "You don't have permission to add/edit tasks for this ticket");
                return response;
            }
            task.setTicket(ticket);
            
            if (task.getId() == null) {
                if (task.getStatus() == null) task.setStatus("OPEN");
                if (task.getStartDate() == null) task.setStartDate(java.time.LocalDate.now());
                task.setCompanyCode(ticket.getCompanyCode());
                
                String currentUsername = (String) session.getAttribute("username");
                if (currentUsername == null) currentUsername = (String) session.getAttribute("userId");
                if (currentUsername == null) currentUsername = "System";
                task.setLastModifiedBy(currentUsername);
                task.setLastModifiedAt(java.time.LocalDateTime.now());
            } else {
                TicketTask existingTask = ticketTaskRepository.findById(task.getId())
                        .orElseThrow(() -> new RuntimeException("Task not found"));
                        
                String oldStatus = existingTask.getStatus();
                
                task.setTicket(existingTask.getTicket());
                if (task.getStartDate() == null) task.setStartDate(existingTask.getStartDate());
                task.setCompanyCode(existingTask.getCompanyCode() != null ? existingTask.getCompanyCode() : ticket.getCompanyCode());
                
                // Notify if a CLOSED task was edited
                if ("CLOSED".equals(oldStatus)) {
                    String username = (String) session.getAttribute("username");
                    if (username == null) username = "System";
                    String userId = (String) session.getAttribute("userId");
                    if (userId == null) userId = "System";
                    
                    String notifyUser = ticket.getAssigneeUserCode();
                    if (notifyUser == null || notifyUser.isEmpty()) {
                        notifyUser = ticket.getUserCode(); // fallback to creator
                    }
                    if (notifyUser != null && !notifyUser.isEmpty()) {
                        notificationService.createTicketTaskEditNotification(
                            ticket, 
                            notifyUser, 
                            userId, 
                            username, 
                            task.getTaskType()
                        );
                    }
                }
                
                // Track modification history
                String currentUsername = (String) session.getAttribute("username");
                if (currentUsername == null) currentUsername = (String) session.getAttribute("userId");
                if (currentUsername == null) currentUsername = "System";
                task.setLastModifiedBy(currentUsername);
                task.setLastModifiedAt(java.time.LocalDateTime.now());
            }
            
            TicketTask savedTask = ticketTaskRepository.save(task);
            
            // Notify creator if the person updating/creating the task is not the creator
            try {
                String currentUsername = (String) session.getAttribute("username");
                if (currentUsername == null) currentUsername = (String) session.getAttribute("userId");
                if (currentUsername == null) currentUsername = "System";
                
                String creator = ticket.getUserCode() != null ? ticket.getUserCode().trim().toLowerCase() : null;
                if (creator != null && !creator.equalsIgnoreCase(currentUsername.trim().toLowerCase())) {
                    notificationService.createTicketUpdateNotification(
                        ticket,
                        creator,
                        currentUsername.trim().toLowerCase(),
                        currentUsername,
                        "Task '" + task.getTaskType() + "' was " + (task.getId() == null ? "added" : "updated")
                    );
                }
            } catch (Exception e) {
                System.err.println("Failed to send task update notification to creator: " + e.getMessage());
            }
            
            response.put("success", true);
            response.put("message", "Task saved successfully");
            response.put("taskId", savedTask.getId());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to save task: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        try {
            TicketTask task = ticketTaskRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Task not found"));
            Long ticketId = task.getTicket().getId();
            Ticket ticket = task.getTicket();
            
            if (!canEditTicket(ticket, session)) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to delete tasks for this ticket");
                return "redirect:/api/tickets/view/" + ticketId;
            }
            
            String currentUsername = (String) session.getAttribute("username");
            if (currentUsername == null) currentUsername = (String) session.getAttribute("userId");
            if (currentUsername == null) currentUsername = "System";
            
            ticketTaskRepository.delete(task);
            
            // Notify creator
            try {
                String creator = ticket.getUserCode() != null ? ticket.getUserCode().trim().toLowerCase() : null;
                if (creator != null && !creator.equalsIgnoreCase(currentUsername.trim().toLowerCase())) {
                    notificationService.createTicketUpdateNotification(
                        ticket,
                        creator,
                        currentUsername.trim().toLowerCase(),
                        currentUsername,
                        "Task '" + task.getTaskType() + "' was deleted"
                    );
                }
            } catch (Exception e) {
                System.err.println("Failed to send task deletion notification to creator: " + e.getMessage());
            }
            
            redirectAttributes.addFlashAttribute("success", "Task deleted successfully!");
            return "redirect:/api/tickets/view/" + ticketId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete task: " + e.getMessage());
            return "redirect:/api/tickets/list";
        }
    }

    @PostMapping("/ajax/permanent-delete/{id}")
    @ResponseBody
    public java.util.Map<String, Object> ajaxPermanentlyDeleteTicket(
            @PathVariable Long id,
            HttpSession session) {
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        String role = (String) session.getAttribute("role");
        if (!"BUSINESS_MANAGER".equals(role)) {
            response.put("success", false);
            response.put("message", "You don't have permission to permanently delete tickets");
            return response;
        }
        
        try {
            ticketService.permanentlyDeleteTicket(id);
            response.put("success", true);
            response.put("message", "Ticket permanently deleted");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/tasks/reminders")
    @ResponseBody
    public java.util.Map<String, Object> getTaskReminders(HttpSession session) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            response.put("success", false);
            response.put("message", "User not logged in");
            return response;
        }

        try {
            List<TicketTask> incompleteTasks = ticketTaskRepository.findByPerformedByAndStatusNot(userId, "CLOSED");
            
            // Format for frontend
            List<java.util.Map<String, Object>> taskList = new java.util.ArrayList<>();
            for (TicketTask t : incompleteTasks) {
                java.util.Map<String, Object> taskMap = new java.util.HashMap<>();
                taskMap.put("id", t.getId());
                taskMap.put("ticketId", t.getTicket() != null ? t.getTicket().getId() : null);
                taskMap.put("ticketNo", t.getTicket() != null ? t.getTicket().getTicketNo() : "Unknown");
                taskMap.put("taskType", t.getTaskType());
                taskMap.put("status", t.getStatus());
                taskMap.put("startDate", t.getStartDate() != null ? t.getStartDate().toString() : "Unknown");
                taskList.add(taskMap);
            }
            
            response.put("success", true);
            response.put("tasks", taskList);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to fetch reminders: " + e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/admin/all-tasks")
    @ResponseBody
    public java.util.Map<String, Object> getAllActiveTasks(HttpSession session) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        String role = (String) session.getAttribute("role");
        String companyCode = (String) session.getAttribute("companyCode");
        
        System.out.println("DEBUG: getAllActiveTasks called. Role: " + role + ", Company: " + companyCode);

        if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
            response.put("success", false);
            response.put("message", "Unauthorized: Admin role required");
            return response;
        }

        try {
            List<TicketTask> activeTasks = ticketTaskRepository.findActiveTasks();
            System.out.println("DEBUG: findActiveTasks returned " + (activeTasks != null ? activeTasks.size() : "NULL") + " tasks");
            
            List<java.util.Map<String, Object>> taskList = new java.util.ArrayList<>();
            if (activeTasks != null) {
                for (TicketTask t : activeTasks) {
                    java.util.Map<String, Object> taskMap = new java.util.HashMap<>();
                    taskMap.put("id", t.getId());
                    taskMap.put("ticketNo", t.getTicket() != null ? t.getTicket().getTicketNo() : "Unknown");
                    taskMap.put("ticketId", t.getTicket() != null ? t.getTicket().getId() : null);
                    taskMap.put("taskType", t.getTaskType());
                    taskMap.put("status", t.getStatus());
                    taskMap.put("performedBy", t.getPerformedBy());
                    taskMap.put("startDate", t.getStartDate() != null ? t.getStartDate().toString() : "");
                    taskList.add(taskMap);
                }
            }
            
            response.put("success", true);
            response.put("tasks", taskList);
        } catch (Exception e) {
            e.printStackTrace(); // Still log to server console
            response.put("success", false);
            response.put("message", "Backend Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
        return response;
    }
    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadTicket(@PathVariable Long id, HttpSession session) throws DocumentException {
        Ticket ticket = ticketService.getById(id);
        List<TicketComment> comments = ticketService.getComments(id);
        List<TicketTask> tasks = ticket.getTasks();

        String logoFilename = (String) session.getAttribute("companyLogo");
        byte[] pdfData = reportExportService.exportTicketToPDF(ticket, comments, tasks, logoFilename);
        String fileName = "Ticket_" + ticket.getTicketNo() + ".pdf";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfData.length)
            .body(new ByteArrayResource(pdfData));
    }

    private String formatRole(String role) {
        if (role == null) return null;
        switch (role.toUpperCase()) {
            case "ADMIN": return "Admin";
            case "BUSINESS_MANAGER": return "Business Manager";
            case "TESTER": return "Tester";
            case "DEVELOPER": return "Developer";
            case "GUEST": return "Guest";
            default:
                String[] words = role.split("_");
                StringBuilder sb = new StringBuilder();
                for (String w : words) {
                    if (w.isEmpty()) continue;
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(Character.toUpperCase(w.charAt(0)))
                      .append(w.substring(1).toLowerCase());
                }
                return sb.toString();
        }
    }

    private boolean canEditTicket(Ticket ticket, HttpSession session) {
        if (ticket == null || session == null) {
            return false;
        }
        
        String role = (String) session.getAttribute("role");
        if ("ADMIN".equalsIgnoreCase(role) || "BUSINESS_MANAGER".equalsIgnoreCase(role)) {
            return true;
        }

        String userId = (String) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        String email = (String) session.getAttribute("email");

        if (userId == null && username == null && email == null) {
            return false;
        }

        // Check if user is the creator
        String creatorCode = ticket.getUserCode();
        String createdBy = ticket.getCreatedBy();
        if (isUserMatch(userId, username, email, creatorCode) || isUserMatch(userId, username, email, createdBy)) {
            return true;
        }

        // Check if user is an assignee
        String assignee1 = ticket.getAssigneeUserCode();
        String assignee2 = ticket.getAssignee2UserCode();
        String assignee3 = ticket.getAssignee3UserCode();
        String assignedTo = ticket.getAssignedTo();
        if (isUserMatch(userId, username, email, assignee1) || 
            isUserMatch(userId, username, email, assignee2) || 
            isUserMatch(userId, username, email, assignee3) || 
            isUserMatch(userId, username, email, assignedTo)) {
            return true;
        }

        return false;
    }

    private boolean isUserMatch(String sessionUserId, String sessionUsername, String sessionEmail, String ticketUserVal) {
        if (ticketUserVal == null || ticketUserVal.trim().isEmpty()) {
            return false;
        }
        String val = ticketUserVal.trim();
        if (sessionUserId != null && sessionUserId.trim().equalsIgnoreCase(val)) {
            return true;
        }
        if (sessionUsername != null && sessionUsername.trim().equalsIgnoreCase(val)) {
            return true;
        }
        if (sessionEmail != null && sessionEmail.trim().equalsIgnoreCase(val)) {
            return true;
        }
        return false;
    }

    private boolean isDeptMatch(String userDept, String targetDept, List<Department> deptList) {
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
            for (Department d : deptList) {
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
}
