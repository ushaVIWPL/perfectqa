package com.example.demo.controller;

import com.example.demo.entity.SupportIssueType;
import com.example.demo.repo.SupportIssueTypeRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/support-issue-types")
@RequiredArgsConstructor
public class AdminSupportIssueTypeController {

    private final SupportIssueTypeRepository supportIssueTypeRepository;

    /** Serve the HTML page (loaded dynamically into the admin panel's #dynamicContent) */
    @GetMapping
    public String supportIssueTypesPage(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return "redirect:/admin";
        }
        return "admin-support-issue-types";
    }

    /** List all support issue types as JSON */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<SupportIssueType>> list(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(supportIssueTypeRepository.findAll());
    }

    /** Create a new support issue type */
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, String> body,
            HttpSession session) {

        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access denied"));
        }

        String name = body.getOrDefault("name", "").trim();
        if (name.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Issue type name is required"));
        }

        Optional<SupportIssueType> existing = supportIssueTypeRepository.findByName(name);
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Support issue type \"" + name + "\" already exists"));
        }

        SupportIssueType sit = new SupportIssueType();
        sit.setName(name);
        SupportIssueType saved = supportIssueTypeRepository.save(sit);

        return ResponseEntity.ok(Map.of("success", true, "message", "Support issue type created successfully", "id", saved.getId(), "name", saved.getName()));
    }

    /** Update an existing support issue type */
    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access denied"));
        }

        String name = body.getOrDefault("name", "").trim();
        if (name.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Issue type name is required"));
        }

        Optional<SupportIssueType> opt = supportIssueTypeRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check duplicate name (excluding self)
        Optional<SupportIssueType> duplicate = supportIssueTypeRepository.findByName(name);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Support issue type \"" + name + "\" already exists"));
        }

        SupportIssueType sit = opt.get();
        sit.setName(name);
        supportIssueTypeRepository.save(sit);

        return ResponseEntity.ok(Map.of("success", true, "message", "Support issue type updated successfully"));
    }

    /** Delete a support issue type */
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable Long id,
            HttpSession session) {

        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access denied"));
        }

        if (!supportIssueTypeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        supportIssueTypeRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Support issue type deleted successfully"));
    }
}
