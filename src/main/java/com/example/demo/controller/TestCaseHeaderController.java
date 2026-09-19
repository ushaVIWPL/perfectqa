package com.example.demo.controller;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.ScenarioActivities;
import com.example.demo.entity.TestCaseHeader;
import com.example.demo.service.ScenarioActivitiesService;
import com.example.demo.service.TestCaseHeaderService;
import com.example.demo.service.UserAccountService;
import com.example.demo.entity.UserAccount;
import com.example.demo.util.RoleAccessUtil;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/api/transactions")
public class TestCaseHeaderController {

    private final TestCaseHeaderService testCaseHeaderService;
    private final ScenarioActivitiesService scenarioActivitiesService;
    private final UserAccountService userAccountService;

    @Autowired
    public TestCaseHeaderController(TestCaseHeaderService testCaseHeaderService,
                                    ScenarioActivitiesService scenarioActivitiesService,
                                    UserAccountService userAccountService) {
        this.testCaseHeaderService = testCaseHeaderService;
        this.scenarioActivitiesService = scenarioActivitiesService;
        this.userAccountService = userAccountService;
    }

    @ModelAttribute("companyUsers")
    public List<UserAccount> populateCompanyUsers(HttpSession session) {
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode != null && !companyCode.isEmpty()) {
            return userAccountService.getUsersByCompanyCode(companyCode);
        }
        return java.util.Collections.emptyList();
    }

    @GetMapping("/testcaseheaders")
    public String listHeaders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "combinedKey") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            HttpSession session, 
            Model model) {
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "Company Code not found in session.");
            return "testheaderlist";
        }

        // Use pagination for better performance
        org.springframework.data.domain.Page<TestCaseHeader> headersPage;
        if (search != null && !search.trim().isEmpty()) {
            headersPage = testCaseHeaderService.searchByCompanyCode(companyCode, search, page, size, sortBy, sortDir);
        } else {
            headersPage = testCaseHeaderService.getByCompanyCode(companyCode, page, size, sortBy, sortDir);
        }

        model.addAttribute("testheaders", headersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", headersPage.getTotalPages());
        model.addAttribute("totalItems", headersPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("companyCode", companyCode);
        
        // Add metrics for dashboard boxes
        model.addAttribute("testersCount", testCaseHeaderService.getDistinctTestedByCount(companyCode));
        model.addAttribute("transactionKeysCount", testCaseHeaderService.getDistinctTransactionKeysCount(companyCode));
        
        Map<Long, Boolean> hasChildrenMap = new java.util.HashMap<>();
        for (TestCaseHeader h : headersPage.getContent()) {
            hasChildrenMap.put(h.getId(), testCaseHeaderService.hasChildren(h.getId()));
        }
        model.addAttribute("hasChildrenMap", hasChildrenMap);
        
        // Add role information for template
        model.addAttribute("canAddEdit", RoleAccessUtil.canAddEdit(session));
        
        return "testheaderlist";
    }

    @GetMapping("/addtestcaseheader/{transactionKey}/{companyCode}")
    public String showAddForm(@PathVariable String transactionKey,
                              @PathVariable String companyCode,
                              HttpSession session,
                              Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/loginform";
        }

        session.setAttribute("companyCode", companyCode);

        TestCaseHeader header = new TestCaseHeader();
        header.setTransactionKey(transactionKey);
        header.setCompanyCode(companyCode);

        model.addAttribute("testCaseHeader", header);
        model.addAttribute("editMode", false);

        return "addtestcaseheader";
    }


    @PostMapping("/savetestheader")
    public String saveTestCaseHeader(
            @ModelAttribute TestCaseHeader header,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "deleteImages", required = false) java.util.List<String> deleteImages,
            HttpSession session,
            Model model) {

        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "Company code missing in session.");
            model.addAttribute("testCaseHeader", header);
            boolean isEdit = header.getId() != null;
            model.addAttribute("editMode", isEdit);
            if (isEdit) {
                populateEditModeImages(header, model);
            }
            return "addtestcaseheader";
        }

        boolean isEdit = header.getId() != null;

        /* ===================== EDIT MODE ===================== */
        if (isEdit) {
            Optional<TestCaseHeader> existingOpt = testCaseHeaderService.findById(header.getId());
            if (existingOpt.isEmpty()) {
                model.addAttribute("error", "Test Case Header not found.");
                model.addAttribute("testCaseHeader", header);
                model.addAttribute("editMode", true);
                populateEditModeImages(header, model);
                return "addtestcaseheader";
            }

            TestCaseHeader existing = existingOpt.get();
            if (!companyCode.equals(existing.getCompanyCode())) {
                model.addAttribute("error", "You do not have permission to edit this test case header.");
                model.addAttribute("testCaseHeader", header);
                model.addAttribute("editMode", true);
                populateEditModeImages(header, model);
                return "addtestcaseheader";
            }

            header.setTransactionKey(existing.getTransactionKey());
            header.setCompanyCode(companyCode);
        }
        /* ===================== ADD MODE ===================== */
        else {
            if (header.getTransactionKey() == null || header.getTransactionKey().isEmpty()) {
                model.addAttribute("error", "Transaction Key is required.");
                model.addAttribute("testCaseHeader", header);
                model.addAttribute("editMode", false);
                return "addtestcaseheader";
            }

            Optional<ScenarioActivities> scenarioActivityOpt =
                    scenarioActivitiesService.getActivityByTransactionKey(header.getTransactionKey());

            if (scenarioActivityOpt.isEmpty()) {
                model.addAttribute("error", "Scenario Activity not found.");
                model.addAttribute("testCaseHeader", header);
                model.addAttribute("editMode", false);
                return "addtestcaseheader";
            }

            ScenarioActivities scenarioActivity = scenarioActivityOpt.get();

            if (scenarioActivity.getId() != null
                    && scenarioActivity.getId().getBusinessScenarioId() != null
                    && !companyCode.equals(
                            scenarioActivity.getId().getBusinessScenarioId().getCompanyCode())) {
                model.addAttribute("error", "Invalid Scenario Activity.");
                model.addAttribute("testCaseHeader", header);
                model.addAttribute("editMode", false);
                return "addtestcaseheader";
            }

            header.setScenarioActivity(scenarioActivity);

            if (header.getTestCaseNo() != null && !header.getTestCaseNo().isEmpty()) {
                boolean exists =
                        testCaseHeaderService.existsByTransactionKeyAndTestCaseNo(header.getTransactionKey(), header.getTestCaseNo(), companyCode);
                if (exists) {
                    model.addAttribute("error", "Test case header already exists.");
                    model.addAttribute("testCaseHeader", header);
                    model.addAttribute("editMode", false);
                    return "addtestcaseheader";
                }
            }

            header.setCompanyCode(companyCode);
        }

        /* ===================== FILE UPLOAD ===================== */
        String uploadDir = "uploads/screenshots/";
        Set<String> imageSet = new LinkedHashSet<>();

        // Load existing images and filter deleted ones
        if (isEdit) {
            Optional<TestCaseHeader> existingOpt = testCaseHeaderService.findById(header.getId());
            if (existingOpt.isPresent()
                    && existingOpt.get().getScreenshotPaths() != null
                    && !existingOpt.get().getScreenshotPaths().isEmpty()) {

                String[] existingArray = existingOpt.get().getScreenshotPaths().split(",");
                for (String img : existingArray) {
                    String trimmed = img.trim();
                    String filename = trimmed;
                    int lastSlashIdx = Math.max(trimmed.lastIndexOf('/'), trimmed.lastIndexOf('\\'));
                    if (lastSlashIdx >= 0) {
                        filename = trimmed.substring(lastSlashIdx + 1);
                    }
                    if (deleteImages == null || !deleteImages.contains(filename)) {
                        imageSet.add(trimmed);
                    } else {
                        // delete file
                        try {
                            Path path = Paths.get(uploadDir, filename);
                            Files.deleteIfExists(path);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if ((files != null && files.length > 0) || (deleteImages != null && !deleteImages.isEmpty())) {
            try {
                if (files != null) {
                    for (MultipartFile file : files) {
                        if (!file.isEmpty()) {
                            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                            Path path = Paths.get(uploadDir, fileName);
                            Files.createDirectories(path.getParent());
                            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                            imageSet.add(fileName);
                        }
                    }
                }
            } catch (Exception e) {
                model.addAttribute("error", "Failed to upload screenshots: " + e.getMessage());
                model.addAttribute("testCaseHeader", header);
                model.addAttribute("editMode", isEdit);
                if (isEdit) {
                    populateEditModeImages(header, model);
                }
                return "addtestcaseheader";
            }

            if (!imageSet.isEmpty()) {
                header.setScreenshotPaths(String.join(",", imageSet));
            } else {
                header.setScreenshotPaths(null);
            }
        } else if (isEdit) {
            // Keep existing screenshot paths if no changes
            Optional<TestCaseHeader> existingOpt = testCaseHeaderService.findById(header.getId());
            existingOpt.ifPresent(e -> header.setScreenshotPaths(e.getScreenshotPaths()));
        }

        /* ===================== PRESERVE SCENARIO ACTIVITY ===================== */
        if (isEdit) {
            Optional<TestCaseHeader> existingOpt = testCaseHeaderService.findById(header.getId());
            existingOpt.ifPresent(e -> header.setScenarioActivity(e.getScenarioActivity()));
        }

        /* ===================== SAVE ===================== */
        testCaseHeaderService.save(header);

        if (isEdit) {
            return "redirect:/api/transactions/testcaseheaders";
        } else {
            model.addAttribute("message", "Test Case Header saved successfully!");
            TestCaseHeader newHeader = new TestCaseHeader();
            newHeader.setTransactionKey(header.getTransactionKey());
            newHeader.setCompanyCode(header.getCompanyCode());
            model.addAttribute("testCaseHeader", newHeader);
            model.addAttribute("editMode", false);
            return "addtestcaseheader";
        }
    }


    // View test case header details (read-only, available for all roles)
    @GetMapping("/viewtestcaseheader/{combinedKey}")
    public String viewTestCaseHeader(@PathVariable String combinedKey,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (session.getAttribute("userId") == null) {
            return "redirect:/loginform";
        }

        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Company code not found in session. Please login again.");
            return "redirect:/api/transactions/testcaseheaders";
        }
        
        // Find the test case header by combined key
        Optional<TestCaseHeader> headerOpt = testCaseHeaderService.findByCombinedKey(combinedKey);
        if (headerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Test Case Header with Combined Key '" + combinedKey + "' not found.");
            return "redirect:/api/transactions/testcaseheaders";
        }
        
        TestCaseHeader header = headerOpt.get();
        
        // Verify the header belongs to the user's company
        if (!companyCode.equals(header.getCompanyCode())) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to view this test case header.");
            return "redirect:/api/transactions/testcaseheaders";
        }
        
        model.addAttribute("testCaseHeader", header);
        
        // Add role information for template
        model.addAttribute("canAddEdit", RoleAccessUtil.canAddEdit(session));
        
        return "view-test-case-header";
    }

    // Show edit form by Combined Key (simplified - gets company code from session)
    @GetMapping("/edittestcaseheader/{combinedKey}")
    public String showEditTestCaseHeaderByCombinedKey(@PathVariable String combinedKey,
                                                      HttpSession session,
                                                      Model model) {
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "Company code not found in session. Please login again.");
            return "redirect:/api/transactions/testcaseheaders";
        }
        
        // Find the test case header by combined key
        Optional<TestCaseHeader> headerOpt = testCaseHeaderService.findByCombinedKey(combinedKey);
        if (headerOpt.isEmpty()) {
            model.addAttribute("error", "Test Case Header with Combined Key '" + combinedKey + "' not found.");
            return "redirect:/api/transactions/testcaseheaders";
        }
        
        TestCaseHeader header = headerOpt.get();
        
        // Verify the header belongs to the user's company
        if (!companyCode.equals(header.getCompanyCode())) {
            model.addAttribute("error", "You do not have permission to edit this test case header.");
            return "redirect:/api/transactions/testcaseheaders";
        }
        
        model.addAttribute("testCaseHeader", header);
        model.addAttribute("editMode", true);
        model.addAttribute("companyCode", companyCode);
        populateEditModeImages(header, model);
        
        return "addtestcaseheader";
    }
    
    // Update test case header (for edit mode)
    @PostMapping("/updatetestheader")
    public String updateTestCaseHeader(@ModelAttribute TestCaseHeader header,
                                      @RequestParam(value = "files", required = false) MultipartFile[] files,
                                      @RequestParam(value = "deleteImages", required = false) java.util.List<String> deleteImages,
                                      HttpSession session,
                                      Model model) {
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty()) {
            model.addAttribute("error", "Company code missing in session.");
            model.addAttribute("testCaseHeader", header);
            model.addAttribute("editMode", true);
            populateEditModeImages(header, model);
            return "addtestcaseheader";
        }
        
        // Verify header exists and belongs to company
        Optional<TestCaseHeader> existingOpt = testCaseHeaderService.findById(header.getId());
        if (existingOpt.isEmpty()) {
            model.addAttribute("error", "Test Case Header not found.");
            model.addAttribute("testCaseHeader", header);
            model.addAttribute("editMode", true);
            populateEditModeImages(header, model);
            return "addtestcaseheader";
        }
        
        TestCaseHeader existing = existingOpt.get();
        if (!companyCode.equals(existing.getCompanyCode())) {
            model.addAttribute("error", "You do not have permission to edit this test case header.");
            model.addAttribute("testCaseHeader", header);
            model.addAttribute("editMode", true);
            populateEditModeImages(header, model);
            return "addtestcaseheader";
        }
        
        String uploadDir = "uploads/screenshots/";
        Set<String> imageSet = new LinkedHashSet<>();
        
        // Load existing images and filter deleted ones
        if (existing.getScreenshotPaths() != null && !existing.getScreenshotPaths().isEmpty()) {
            String[] existingArray = existing.getScreenshotPaths().split(",");
            for (String img : existingArray) {
                String trimmed = img.trim();
                String filename = trimmed;
                int lastSlashIdx = Math.max(trimmed.lastIndexOf('/'), trimmed.lastIndexOf('\\'));
                if (lastSlashIdx >= 0) {
                    filename = trimmed.substring(lastSlashIdx + 1);
                }
                if (deleteImages == null || !deleteImages.contains(filename)) {
                    imageSet.add(trimmed);
                } else {
                    // delete file
                    try {
                        Path path = Paths.get(uploadDir, filename);
                        Files.deleteIfExists(path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        // Handle new file uploads
        if (files != null && files.length > 0) {
            try {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                        Path path = Paths.get(uploadDir, fileName);
                        Files.createDirectories(path.getParent());
                        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                        imageSet.add(fileName);
                    }
                }
            } catch (Exception e) {
                model.addAttribute("error", "Failed to upload screenshots: " + e.getMessage());
                model.addAttribute("testCaseHeader", header);
                model.addAttribute("editMode", true);
                populateEditModeImages(header, model);
                return "addtestcaseheader";
            }
        }
        
        if (!imageSet.isEmpty()) {
            header.setScreenshotPaths(String.join(",", imageSet));
        } else {
            header.setScreenshotPaths(null);
        }
        
        // Ensure company code and transaction key are preserved
        header.setCompanyCode(companyCode);
        header.setTransactionKey(existing.getTransactionKey());
        
        // Update scenario activity relationship
        if (existing.getScenarioActivity() != null) {
            header.setScenarioActivity(existing.getScenarioActivity());
        } else {
            // Find scenario activity if not set
            ScenarioActivities scenarioActivity = scenarioActivitiesService
                    .getActivitiesByCompanyCode(companyCode)
                    .stream()
                    .filter(a -> a.getTransactionKey().equals(header.getTransactionKey()))
                    .findFirst()
                    .orElse(null);
            if (scenarioActivity != null) {
                header.setScenarioActivity(scenarioActivity);
            }
        }
        
        // Save the updated header
        testCaseHeaderService.save(header);
        
        return "redirect:/api/transactions/testcaseheaders";
    }

    @GetMapping("/testcaseheaders/exists")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkHeaderExists(
            @RequestParam String transactionKey,
            @RequestParam String testCaseNo,
            HttpSession session) {
        String companyCode = (String) session.getAttribute("companyCode");
        if (companyCode == null || companyCode.isEmpty() || 
            transactionKey == null || transactionKey.isEmpty() || 
            testCaseNo == null || testCaseNo.isEmpty()) {
            return ResponseEntity.ok(Map.of("exists", false));
        }
        
        boolean exists = testCaseHeaderService.existsByTransactionKeyAndTestCaseNo(transactionKey.trim(), testCaseNo.trim(), companyCode);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping("/ajax/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteTestHeader(@PathVariable Long id, HttpSession session) {
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

            Optional<TestCaseHeader> headerOpt = testCaseHeaderService.findById(id);
            if (headerOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Test Case Header not found.");
                return ResponseEntity.ok(response);
            }

            if (testCaseHeaderService.hasChildren(id)) {
                response.put("success", false);
                response.put("message", "Cannot delete because there are dependent transactions.");
                return ResponseEntity.ok(response);
            }

            String combinedKey = headerOpt.get().getCombinedKey();
            testCaseHeaderService.deleteById(id);
            response.put("success", true);
            response.put("message", "Deleted! Test Case Header " + (combinedKey != null ? combinedKey : id) + " deleted successfully.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error deleting test case header: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    private void populateEditModeImages(TestCaseHeader header, Model model) {
        List<String> imagePaths = header.getImagePathsList();
        model.addAttribute("imagePaths", imagePaths);
        model.addAttribute("existingImageCount", imagePaths.size());
    }
}
