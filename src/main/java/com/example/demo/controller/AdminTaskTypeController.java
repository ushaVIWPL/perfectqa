package com.example.demo.controller;

import com.example.demo.entity.TaskType;
import com.example.demo.repo.TaskTypeRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/task-types")
@RequiredArgsConstructor
public class AdminTaskTypeController {

    private final TaskTypeRepository taskTypeRepository;

    /** Serve the HTML page (loaded dynamically into the admin panel's #dynamicContent) */
    @GetMapping
    public String taskTypesPage(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return "redirect:/admin";
        }
        return "admin-task-types";
    }

    /** List all task types as JSON */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<TaskType>> list(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(taskTypeRepository.findAll());
    }

    /** Create a new task type */
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
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Task name is required"));
        }

        Optional<TaskType> existing = taskTypeRepository.findByName(name);
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Task type \"" + name + "\" already exists"));
        }

        TaskType tt = new TaskType();
        tt.setName(name);
        TaskType saved = taskTypeRepository.save(tt);

        return ResponseEntity.ok(Map.of("success", true, "message", "Task type created successfully", "id", saved.getId(), "name", saved.getName()));
    }

    /** Update an existing task type */
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
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Task name is required"));
        }

        Optional<TaskType> opt = taskTypeRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check duplicate name (excluding self)
        Optional<TaskType> duplicate = taskTypeRepository.findByName(name);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Task type \"" + name + "\" already exists"));
        }

        TaskType tt = opt.get();
        tt.setName(name);
        taskTypeRepository.save(tt);

        return ResponseEntity.ok(Map.of("success", true, "message", "Task type updated successfully"));
    }

    /** Delete a task type */
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable Long id,
            HttpSession session) {

        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access denied"));
        }

        if (!taskTypeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        taskTypeRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Task type deleted successfully"));
    }
}
