package com.example.demo.controller;

import com.example.demo.entity.TicketTask;
import com.example.demo.repo.TicketTaskRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TicketTaskController {

    private final TicketTaskRepository ticketTaskRepository;

    @GetMapping("/menu")
    public String myTasksMenu(HttpSession session, Model model) {
        if (session.getAttribute("userId") == null && session.getAttribute("username") == null) {
            return "redirect:/loginform";
        }
        return "menu-my-tasks";
    }

    @GetMapping("/my-tasks")
    public String myTasks(@RequestParam(required = false) String status,
                          @RequestParam(required = false) Boolean all,
                          HttpSession session,
                          Model model) {
                          
        String userId = (String) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        String companyCode = (String) session.getAttribute("companyCode");
        
        if (userId == null && username == null) {
            return "redirect:/loginform";
        }
        
        String performedBy = userId != null ? userId : username;
        List<TicketTask> allTasks;
        boolean isAdminOrManager = "ADMIN".equalsIgnoreCase(role) || "BUSINESS_MANAGER".equalsIgnoreCase(role);
        
        // Default to "all=true" for admins if parameter isn't specified, else use the parameter
        boolean isAllTasks = isAdminOrManager && (all == null ? true : all);

        if (isAllTasks) {
            if (companyCode != null && !companyCode.trim().isEmpty()) {
                allTasks = ticketTaskRepository.findByCompanyCode(companyCode.trim());
                if (allTasks.isEmpty() && "ADMIN".equalsIgnoreCase(role)) {
                    allTasks = ticketTaskRepository.findAll();
                }
            } else {
                allTasks = ticketTaskRepository.findAll();
            }
        } else {
            allTasks = ticketTaskRepository.findByPerformedByIgnoreCase(performedBy);
        }

        // Calculate statistics
        long totalCount = allTasks.size();
        long openCount = allTasks.stream().filter(t -> "OPEN".equalsIgnoreCase(t.getStatus())).count();
        long inProgressCount = allTasks.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
        long closedCount = allTasks.stream().filter(t -> "CLOSED".equalsIgnoreCase(t.getStatus())).count();

        double billableHoursSum = allTasks.stream()
                .mapToDouble(t -> t.getBillableHours() != null ? t.getBillableHours() : 0.0)
                .sum();
        String totalBillableHours = String.format(java.util.Locale.US, "%.1f", billableHoursSum);
        int completionRate = totalCount > 0 ? (int) Math.round(((double) closedCount / totalCount) * 100) : 0;

        // Apply status filter
        List<TicketTask> tasks = allTasks;
        if (status != null && !status.isEmpty()) {
            tasks = tasks.stream()
                .filter(t -> status.equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());
        }

        model.addAttribute("tasks", tasks);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("openCount", openCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("closedCount", closedCount);
        model.addAttribute("totalBillableHours", totalBillableHours);
        model.addAttribute("completionRate", completionRate);
        model.addAttribute("username", performedBy);
        model.addAttribute("isAdminOrManager", isAdminOrManager);
        model.addAttribute("isAllTasks", isAllTasks);

        return "my-tasks";
    }
}
