package com.example.demo.controller;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.CompanyRef;
import com.example.demo.service.CompanyRefService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.servlet.http.HttpSession;

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
    public String saveCompany(@ModelAttribute("company") CompanyRef company,
                              @RequestParam(value = "isEdit", required = false) String isEdit,
                              @RequestParam(value = "originalCompanyCode", required = false) String originalCompanyCode,
                              @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                              HttpSession session,
                              Model model) {
        // Check if company code is provided
        if (company.getCompanyCode() == null || company.getCompanyCode().trim().isEmpty()) {
            model.addAttribute("error", "Company Code is required.");
            model.addAttribute("companies", service.getAll());
            model.addAttribute("company", company);
            model.addAttribute("c", new CompanyRef());
            return "companyList";
        }
        
        // Handle Logo Upload
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                String originalFilename = logoFile.getOriginalFilename();
                String fileName = company.getCompanyCode() + "_" + originalFilename;
                
                // Store image binary bytes directly in DB entity
                company.setLogoData(logoFile.getBytes());
                company.setLogoContentType(logoFile.getContentType());
                company.setLogoPath(fileName);

                // Optional backup copy to disk
                try {
                    Path uploadPath = Paths.get("uploads/logos/");
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(logoFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception fileErr) {
                    System.err.println("Warning: Disk backup save failed: " + fileErr.getMessage());
                }
            } catch (Exception e) {
                model.addAttribute("error", "Failed to process logo: " + e.getMessage());
                model.addAttribute("companies", service.getAll());
                model.addAttribute("company", company);
                model.addAttribute("c", new CompanyRef());
                return "companyList";
            }
        } else if (isEdit != null && "true".equals(isEdit)) {
            // Keep existing logo data if no new file is uploaded
            service.getById(originalCompanyCode).ifPresent(existing -> {
                company.setLogoPath(existing.getLogoPath());
                company.setLogoData(existing.getLogoData());
                company.setLogoContentType(existing.getLogoContentType());
            });
        }
        
        // Check if company with this code already exists
        CompanyRef existingCompany = service.findByCompanyCode(company.getCompanyCode());
        
        // If this is an edit operation
        if (isEdit != null && "true".equals(isEdit)) {
            // If company code is being changed to a different existing company
            if (existingCompany != null && originalCompanyCode != null 
                && !originalCompanyCode.equals(company.getCompanyCode())) {
                model.addAttribute("error", "A company with code '" + company.getCompanyCode() + "' already exists.");
                model.addAttribute("companies", service.getAll());
                model.addAttribute("company", company);
                model.addAttribute("c", new CompanyRef());
                return "companyList";
            }
        } else {
            // This is an add operation - check for duplicates
            if (existingCompany != null) {
                model.addAttribute("error", "A company with code '" + company.getCompanyCode() + "' already exists.");
                model.addAttribute("companies", service.getAll());
                model.addAttribute("company", new CompanyRef());
                model.addAttribute("c", new CompanyRef());
                return "companyList";
            }
        }
        
        service.save(company);
        
        // Refresh active session if the current user's company matches
        if (session != null && session.getAttribute("companyCode") != null) {
            String activeCompanyCode = (String) session.getAttribute("companyCode");
            if (activeCompanyCode.equalsIgnoreCase(company.getCompanyCode())) {
                if (company.getLogoPath() != null) {
                    session.setAttribute("companyLogo", company.getLogoPath());
                }
                if (company.getCompanyName() != null) {
                    session.setAttribute("companyName", company.getCompanyName());
                }
            }
        }
        
        return "redirect:/company/admin/all";
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
    public String deleteCompany(@PathVariable String code, RedirectAttributes redirectAttributes) {
        try {
            service.deleteById(code);
            redirectAttributes.addFlashAttribute("success", "Company '" + code + "' was deleted successfully.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete company '" + code + "' because it has dependent records (such as business scenarios, tickets, projects, departments, or users) associated with it. Please remove or reassign dependent data first.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete company '" + code + "': " + e.getMessage());
        }
        return "redirect:/company/admin/all";
    }
    
    // API endpoint to get all companies as JSON
    @GetMapping("/api/companies")
    @ResponseBody
    public List<CompanyRef> getAllCompaniesJson() {
        return service.getAll();
    }

    // Dynamic endpoint serving company logo image directly from DB bytes
    @GetMapping({"/logo/{code}", "/logo"})
    @ResponseBody
    public ResponseEntity<byte[]> getCompanyLogo(@PathVariable(required = false) String code, HttpSession session) {
        String targetCode = code;
        if (targetCode == null || targetCode.trim().isEmpty() || "null".equalsIgnoreCase(targetCode) || "undefined".equalsIgnoreCase(targetCode)) {
            if (session != null && session.getAttribute("companyCode") != null) {
                targetCode = (String) session.getAttribute("companyCode");
            }
        }

        // If targetCode is still missing/empty, default to the first available company in DB
        if (targetCode == null || targetCode.trim().isEmpty() || "null".equalsIgnoreCase(targetCode) || "undefined".equalsIgnoreCase(targetCode)) {
            List<CompanyRef> allCompanies = service.getAll();
            if (!allCompanies.isEmpty()) {
                targetCode = allCompanies.get(0).getCompanyCode();
            }
        }

        if (targetCode != null && !targetCode.trim().isEmpty() && !"null".equalsIgnoreCase(targetCode)) {
            Optional<CompanyRef> optional = service.getById(targetCode);
            if (optional.isEmpty()) {
                CompanyRef findByCode = service.findByCompanyCode(targetCode);
                if (findByCode != null) {
                    optional = Optional.of(findByCode);
                }
            }

            if (optional.isPresent()) {
                CompanyRef c = optional.get();
                // 1. Try DB binary bytes first
                if (c.getLogoData() != null && c.getLogoData().length > 0) {
                    String contentType = c.getLogoContentType();
                    if (contentType == null || contentType.isEmpty() || "null".equalsIgnoreCase(contentType)) {
                        contentType = "image/png";
                    }
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, contentType)
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache, must-revalidate")
                            .body(c.getLogoData());
                }

                // 2. Try disk file if logoPath is saved in entity
                if (c.getLogoPath() != null && !c.getLogoPath().trim().isEmpty()) {
                    try {
                        Path logoDir = Paths.get("uploads/logos/");
                        Path filePath = logoDir.resolve(c.getLogoPath());
                        if (!Files.exists(filePath)) {
                            filePath = logoDir.resolve(c.getCompanyCode() + "_" + c.getLogoPath());
                        }
                        if (Files.exists(filePath)) {
                            byte[] fileBytes = Files.readAllBytes(filePath);
                            String contentType = Files.probeContentType(filePath);
                            if (contentType == null) {
                                String lname = filePath.getFileName().toString().toLowerCase();
                                contentType = (lname.endsWith(".jpg") || lname.endsWith(".jpeg")) ? "image/jpeg" : "image/png";
                            }
                            // Save logo back to DB entity
                            try {
                                c.setLogoData(fileBytes);
                                c.setLogoContentType(contentType);
                                service.save(c);
                            } catch (Exception ignored) {}

                            return ResponseEntity.ok()
                                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, must-revalidate")
                                    .body(fileBytes);
                        }
                    } catch (Exception e) {
                        System.err.println("Warning: Could not read logo file from disk: " + e.getMessage());
                    }
                }
            }

            // 3. Search uploads/logos/ directory for matching filename (e.g. {code}_*.png or *.jpg)
            try {
                Path logoDir = Paths.get("uploads/logos/");
                if (Files.exists(logoDir)) {
                    String prefix = targetCode.toLowerCase() + "_";
                    try (var stream = Files.list(logoDir)) {
                        Optional<Path> matchedFile = stream
                                .filter(p -> p.getFileName().toString().toLowerCase().startsWith(prefix))
                                .findFirst();
                        if (matchedFile.isPresent()) {
                            Path filePath = matchedFile.get();
                            byte[] fileBytes = Files.readAllBytes(filePath);
                            String contentType = Files.probeContentType(filePath);
                            if (contentType == null) {
                                String lname = filePath.getFileName().toString().toLowerCase();
                                contentType = (lname.endsWith(".jpg") || lname.endsWith(".jpeg")) ? "image/jpeg" : "image/png";
                            }

                            if (optional.isPresent()) {
                                try {
                                    CompanyRef c = optional.get();
                                    c.setLogoData(fileBytes);
                                    c.setLogoContentType(contentType);
                                    c.setLogoPath(filePath.getFileName().toString());
                                    service.save(c);
                                } catch (Exception ignored) {}
                            }

                            return ResponseEntity.ok()
                                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, must-revalidate")
                                    .body(fileBytes);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Warning: Directory search for logo failed: " + e.getMessage());
            }
        }

        // 4. Check if any company in DB has logoData as fallback
        try {
            List<CompanyRef> allCompanies = service.getAll();
            for (CompanyRef comp : allCompanies) {
                if (comp.getLogoData() != null && comp.getLogoData().length > 0) {
                    String contentType = comp.getLogoContentType();
                    if (contentType == null || contentType.isEmpty() || "null".equalsIgnoreCase(contentType)) {
                        contentType = "image/png";
                    }
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, contentType)
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache, must-revalidate")
                            .body(comp.getLogoData());
                }
            }
        } catch (Exception ignored) {}

        // 5. Fallback: Serve static IWPL.png asset from classpath
        try {
            Resource resource = new ClassPathResource("static/img/IWPL.png");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    byte[] defaultBytes = is.readAllBytes();
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, "image/png")
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache, must-revalidate")
                            .body(defaultBytes);
                }
            }
        } catch (Exception ignored) {}

        return ResponseEntity.notFound().build();
    }

}
