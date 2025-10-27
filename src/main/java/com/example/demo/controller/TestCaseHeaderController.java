package com.example.demo.controller;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.ScreenshotDTO;
import com.example.demo.entity.TestCaseHeader;
import com.example.demo.entity.TestCaseTransaction;
import com.example.demo.repo.TestCaseHeaderRepository;
import com.example.demo.service.TestCaseHeaderService;

import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/api/transactions")
public class TestCaseHeaderController {

	@Autowired
    private TestCaseHeaderService testCaseHeaderService;
	
	@Autowired
    private TestCaseHeaderRepository testCaseHeaderRepo;

	
    @GetMapping("/addtestheaderr")
    public String showQaAdminMenu() {
        return "addtestheader";  // Should map to a Thymeleaf template named addtestheader.html
    }
    
    @GetMapping("/edittestheader")
    public String showEditform() {
        return "edittestheader";  // Should map to a Thymeleaf template named addtestheader.html
    }
    
   
    
    
    @GetMapping("/addtestheader")
    public String showTestHeaderForm(@RequestParam("transactionKey") String transactionKey, Model model) {
        TestCaseHeader testCaseHeader = new TestCaseHeader();
        testCaseHeader.setTransactionKey(transactionKey);
        model.addAttribute("testCaseHeader", testCaseHeader);
        model.addAttribute("editMode", false); // ✅ mark add mode
        return "addtestheader";
    }

    @GetMapping("/edittestheader/{combinedKey}")
    public String showEditHeaderForm(@PathVariable("combinedKey") String combinedKey, Model model) throws IOException {
        TestCaseHeader header = testCaseHeaderRepo.findByCombinedKey(combinedKey)
                .orElseThrow(() -> new RuntimeException("Header not found"));

        List<ScreenshotDTO> screenshots = new ArrayList<>();
        for (String fileName : header.getScreenshotList()) {
            Path filePath = Paths.get("uploads/testheaders", fileName.trim());
            if (Files.exists(filePath)) {
                byte[] bytes = Files.readAllBytes(filePath);
                String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
                screenshots.add(new ScreenshotDTO(fileName.trim(), base64));
            }
        }

        model.addAttribute("testCaseHeader", header);
        model.addAttribute("screenshots", screenshots);

        model.addAttribute("editMode", true);
        return "edittestheader";
    }


    @GetMapping("/deleteHeaderScreenshot")
    public String deleteHeaderScreenshot(@RequestParam String combinedKey,
                                         @RequestParam String fileName,
                                         RedirectAttributes redirectAttrs) {
        // Decode the URL-encoded fileName
        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        System.out.println("Decoded File Name: " + fileName);

        Optional<TestCaseHeader> optionalHeader = testCaseHeaderService.getHeaderByCombinedKey(combinedKey);
        if (optionalHeader.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Header not found");
            return "redirect:/api/transactions/edittestheader/" + combinedKey;
        }

        TestCaseHeader header = optionalHeader.get();
        List<String> paths = new ArrayList<>(Arrays.asList(header.getScreenshotPaths().split(",")));

        if (!paths.remove(fileName)) {
            System.out.println("File not found in header's list: " + fileName);
        } else {
            header.setScreenshotPaths(String.join(",", paths));
            testCaseHeaderService.saveHeader(header);
            System.out.println("Deleted screenshot: " + fileName);
            redirectAttrs.addFlashAttribute("message", "Screenshot deleted successfully");
        }

        return "redirect:/api/transactions/edittestheader/" + combinedKey;
    }
    
    
    
    
    @PostMapping("/savetestheader")
    public String saveTestCaseHeader(@ModelAttribute TestCaseHeader testCaseHeader, Model model) {
        testCaseHeader.generateCombinedKey();

        // folder where files are saved
        String uploadDir = "uploads/testheaders/";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        List<String> storedFiles = new ArrayList<>();

        if (testCaseHeader.getFiles() != null) {
            for (MultipartFile file : testCaseHeader.getFiles()) {
                if (!file.isEmpty()) {
                    try {
                        String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                        Path path = Paths.get(uploadDir, uniqueName);
                        Files.write(path, file.getBytes()); // save file to disk
                        storedFiles.add(uniqueName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (!storedFiles.isEmpty()) {
            // save filenames as comma-separated string in DB
            testCaseHeader.setScreenshotPaths(String.join(",", storedFiles));
        }

        testCaseHeaderService.saveTestCaseHeader(testCaseHeader);
        model.addAttribute("message", "Test Case Header saved successfully!");
        return "addtestheader";
    }
    
    
    
    

    
    
    @GetMapping("/viewTestHeader/{id}")
    public String viewTestHeader(@PathVariable String id, Model model) {
        TestCaseHeader header = testCaseHeaderService.findById(id)
            .orElseThrow(() -> new RuntimeException("Not found: " + id));
        model.addAttribute("header", header);
        return "testheaderlist"; // thymeleaf page
    }


    
    
    
 // ✅ Create TestCase with file upload
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<TestCaseHeader> createTestCase(
            @RequestPart("data") TestCaseHeader header,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {

        TestCaseHeader saved = testCaseHeaderService.saveTestCase(header, files);
        return ResponseEntity.ok(saved);
    }

  
    
  
    @ResponseBody
    @PostMapping("/savetestheader-json")
    public String saveTestHeaderFromJson(@RequestBody TestCaseHeader testCaseHeader) {
        testCaseHeaderService.saveTestCaseHeader(testCaseHeader);
        return "Saved TestCaseHeader with transactionKey: " + testCaseHeader.getTransactionKey();
    }
    
	/*
	 * @GetMapping("/testcaseheaders") public String listTestCaseHeaders(Model
	 * model) { List<TestCaseHeader> testcases =
	 * testCaseHeaderService.getAllTestCaseHeaders();
	 * model.addAttribute("testcases", testcases); // Make sure name matches HTML
	 * return "testheaderlist"; // This should match the .html file name }
	 * 
	 */
    
  
	/*
	 * @Value("${app.upload-dir:uploads}") private String uploadRoot;
	 */
    
    
    private String encodeFileToBase64(Path filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(filePath);
        return Base64.getEncoder().encodeToString(bytes);
    }
 
    @Value("${app.upload-dir:uploads/testheaders}")
    private String uploadRoot;

    @GetMapping("/testcaseheaders")
    public String listTestCaseHeaders(Model model, HttpSession session) throws IOException {
        List<TestCaseHeader> testcases = testCaseHeaderService.findAll();

        for (TestCaseHeader tc : testcases) {
            List<String> base64List = new ArrayList<>();
            for (String fileName : tc.getScreenshotList()) {
                Path filePath = Paths.get(uploadRoot, fileName.trim());
                if (Files.exists(filePath)) {
                    byte[] bytes = Files.readAllBytes(filePath);
                    String ext = fileName.toLowerCase().endsWith(".png") ? "png" : "jpeg";
                    base64List.add("data:image/" + ext + ";base64," + Base64.getEncoder().encodeToString(bytes));
                }
            }
            tc.setScreenshotBase64List(base64List);
        }

        model.addAttribute("testcases", testcases);

        // ✅ fetch role from session
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null) userRole = "tester"; // fallback for testing
        model.addAttribute("userRole", userRole);

        return "testheaderlist";
    }

    
    
    @GetMapping("/api/testcaseheaders")
    @ResponseBody
    public List<TestCaseHeader> getTestCasesJson() throws IOException {
        List<TestCaseHeader> testcases = testCaseHeaderService.findAll();

        for (TestCaseHeader tc : testcases) {
            List<String> base64List = new ArrayList<>();

            if (tc.getScreenshotPaths() != null && !tc.getScreenshotPaths().isBlank()) {
                for (String fileName : tc.getScreenshotPaths().split(",")) {
                    // Correct absolute path
                    Path filePath = Paths.get("E:/testingtool/testingtool/uploads/testheaders", fileName.trim());
                    
                    if (Files.exists(filePath)) {
                        byte[] bytes = Files.readAllBytes(filePath);
                        String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
                        base64List.add(base64);
                    } else {
                        System.out.println("File not found: " + filePath.toAbsolutePath());
                    }
                }
            }

            tc.setScreenshotBase64List(base64List);
        }

        return testcases;
    }

    

    
    
    
    @GetMapping("/check-scenario")
    @ResponseBody
    public ResponseEntity<Boolean> checkScenarioExists(@RequestParam("scenario") String scenario) {
        boolean exists = testCaseHeaderService.findByScenario(scenario).isPresent();
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/edittestheader")
    public ResponseEntity<TestCaseHeader> editTestHeader(
            @RequestParam String scenario,
            @RequestBody TestCaseHeader updatedData) {

        TestCaseHeader updated = testCaseHeaderService.updateByScenario(scenario, updatedData);
        return ResponseEntity.ok(updated);
    }



    @PostMapping("/savetestheaderedit")
    public String saveTestCaseHeaderEdit(
            @ModelAttribute TestCaseHeader testCaseHeader,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "existingFileNames", required = false) String existingFileNames,
            Model model) {

        System.out.println("✅ Updating TestCaseHeader...");
        System.out.println("➡ Combined Key: " + testCaseHeader.getCombinedKey());
        System.out.println("➡ Transaction Key: " + testCaseHeader.getTransactionKey());
        System.out.println("➡ Scenario: " + testCaseHeader.getScenario());
        System.out.println("➡ Description: " + testCaseHeader.getDescription());
        System.out.println("➡ Existing Files (from form): " + existingFileNames);
        System.out.println("➡ New Uploaded Files Count: " + (files != null ? files.size() : 0));

        // Upload directory
        String uploadDir = "uploads/testheaders/";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        List<String> allFiles = new ArrayList<>();

        // Preserve existing files
        if (existingFileNames != null && !existingFileNames.isBlank()) {
            allFiles.addAll(Arrays.asList(existingFileNames.split(",")));
        }

        // Save new uploaded files
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                        Path path = Paths.get(uploadDir, uniqueName);
                        Files.write(path, file.getBytes());
                        allFiles.add(uniqueName);
                        System.out.println("📸 Uploaded new file: " + uniqueName);
                    } catch (IOException e) {
                        e.printStackTrace();
                        model.addAttribute("error", "Error saving file: " + file.getOriginalFilename());
                        return "edittestheader";
                    }
                }
            }
        }

        // Save screenshot paths to DB
        String joined = String.join(",", allFiles);
        testCaseHeader.setScreenshotPaths(joined);

        // Persist to DB
        testCaseHeaderService.saveTestCaseHeader(testCaseHeader);
        System.out.println("✅ Saved TestCaseHeader with combined key: " + testCaseHeader.getCombinedKey());

        // Add attributes back to model
        model.addAttribute("testCaseHeader", testCaseHeader);
        model.addAttribute("existingFileNames", joined); // ✅ important
        model.addAttribute("message", "Test case header updated successfully!");

        // Rebuild screenshots for display
        List<Map<String, String>> screenshots = new ArrayList<>();
        for (String fileName : allFiles) {
            Path path = Paths.get(uploadDir, fileName.trim());
            if (Files.exists(path)) {
                try {
                    byte[] bytes = Files.readAllBytes(path);
                    String base64 = Base64.getEncoder().encodeToString(bytes);

                    Map<String, String> fileMap = new HashMap<>();
                    fileMap.put("name", fileName.trim());
                    fileMap.put("base64", "data:image/png;base64," + base64);

                    screenshots.add(fileMap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        model.addAttribute("screenshots", screenshots);

        return "edittestheader";
    }


    
    @PostMapping("/updateTransaction")
    public String updateTransaction(@ModelAttribute TestCaseTransaction txn, Model model) {
    	testCaseHeaderService.updateTransaction(txn); // custom method or use save() if using JpaRepository
        return "redirect:/getall"; // refresh and show all rows again in view mode
    }


}

   

