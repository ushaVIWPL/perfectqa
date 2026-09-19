package com.example.demo.controller;

import com.example.demo.entity.Notification;
import com.example.demo.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Get all notifications page
    @GetMapping
    public String getNotificationsPage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        System.out.println("Getting notifications page for userId: " + userId);
        
        if (userId == null) {
            System.out.println("userId is null, redirecting to login");
            return "redirect:/loginform";
        }

        // CRITICAL: Normalize userId to match database format (lowercase, trimmed)
        // This ensures exact match with recipientUserId stored in notifications
        String normalizedUserId = userId.trim().toLowerCase();
        System.out.println("Normalized userId for query: '" + normalizedUserId + "' (original: '" + userId + "')");

        List<Notification> notifications = notificationService.getNotificationsForUser(normalizedUserId);
        long unreadCount = notificationService.countUnreadNotifications(normalizedUserId);
        
        System.out.println("Found " + notifications.size() + " total notifications and " + unreadCount + " unread for userId: " + normalizedUserId);

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("userRole", session.getAttribute("role"));

        return "notifications";
    }

    // Get unread notifications count (AJAX)
    @GetMapping("/unread-count")
    @ResponseBody
    public Map<String, Object> getUnreadCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String userId = (String) session.getAttribute("userId");
        
        System.out.println("Getting unread count for userId: " + userId);
        
        if (userId == null) {
            System.out.println("userId is null in session");
            response.put("count", 0);
            return response;
        }

        // CRITICAL: Normalize userId to match database format
        String normalizedUserId = userId.trim().toLowerCase();
        System.out.println("Normalized userId for query: '" + normalizedUserId + "'");
        
        long count = notificationService.countUnreadNotifications(normalizedUserId);
        System.out.println("Unread count for " + normalizedUserId + ": " + count);
        response.put("count", count);
        return response;
    }

    // Get unread notifications list (AJAX)
    @GetMapping("/unread")
    @ResponseBody
    public List<Notification> getUnreadNotifications(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        System.out.println("Getting unread notifications for userId: " + userId);
        
        if (userId == null) {
            System.out.println("userId is null in session");
            return List.of();
        }
        
        // CRITICAL: Normalize userId to match database format
        String normalizedUserId = userId.trim().toLowerCase();
        System.out.println("Normalized userId for query: '" + normalizedUserId + "'");
        
        List<Notification> notifications = notificationService.getUnreadNotificationsForUser(normalizedUserId);
        System.out.println("Found " + notifications.size() + " unread notifications for " + normalizedUserId);
        return notifications;
    }

    // Get all notifications (AJAX)
    @GetMapping("/all")
    @ResponseBody
    public List<Notification> getAllNotifications(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return List.of();
        }
        // CRITICAL: Normalize userId to match database format
        String normalizedUserId = userId.trim().toLowerCase();
        return notificationService.getNotificationsForUser(normalizedUserId);
    }

    // Mark notification as read
    @PostMapping("/mark-read/{id}")
    @ResponseBody
    public Map<String, Object> markAsRead(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String userId = (String) session.getAttribute("userId");
        
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Not logged in");
            return response;
        }

        try {
            Notification notification = notificationService.getById(id);
            if (notification != null && notification.getRecipientUserId().equals(userId)) {
                notificationService.markAsRead(id);
                response.put("success", true);
            } else {
                response.put("success", false);
                response.put("message", "Notification not found or unauthorized");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // Mark all notifications as read
    @PostMapping("/mark-all-read")
    @ResponseBody
    public Map<String, Object> markAllAsRead(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String userId = (String) session.getAttribute("userId");
        
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Not logged in");
            return response;
        }

        try {
            int count = notificationService.markAllAsRead(userId);
            response.put("success", true);
            response.put("markedCount", count);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // Delete notification
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteNotification(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String userId = (String) session.getAttribute("userId");
        
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Not logged in");
            return response;
        }

        try {
            Notification notification = notificationService.getById(id);
            if (notification != null && notification.getRecipientUserId().equals(userId)) {
                notificationService.deleteNotification(id);
                response.put("success", true);
            } else {
                response.put("success", false);
                response.put("message", "Notification not found or unauthorized");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // Navigate to notification action URL and mark as read
    @GetMapping("/view/{id}")
    public String viewNotification(@PathVariable Long id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/loginform";
        }

        Notification notification = notificationService.getById(id);
        if (notification != null && notification.getRecipientUserId().equals(userId)) {
            notificationService.markAsRead(id);
            
            // Redirect to the action URL if available
            if (notification.getActionUrl() != null && !notification.getActionUrl().isEmpty()) {
                return "redirect:" + notification.getActionUrl();
            }
        }
        
        return "redirect:/api/notifications";
    }
    
    // Debug endpoint to list all notifications
    @GetMapping("/debug/all")
    @ResponseBody
    public Map<String, Object> debugAllNotifications() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Notification> all = notificationService.getAllNotifications();
            response.put("success", true);
            response.put("count", all.size());
            response.put("notifications", all.stream().map(n -> {
                Map<String, Object> notif = new HashMap<>();
                notif.put("id", n.getId());
                notif.put("recipientUserId", n.getRecipientUserId());
                notif.put("recipientUserIdLength", n.getRecipientUserId() != null ? n.getRecipientUserId().length() : 0);
                notif.put("senderUserId", n.getSenderUserId());
                notif.put("title", n.getTitle());
                notif.put("type", n.getNotificationType());
                notif.put("isRead", n.getIsRead());
                notif.put("createdAt", n.getCreatedAt());
                notif.put("referenceId", n.getReferenceId());
                notif.put("referenceCode", n.getReferenceCode());
                notif.put("message", n.getMessage());
                return notif;
            }).collect(java.util.stream.Collectors.toList()));
            
            // Also add unique recipient user IDs for debugging
            List<String> uniqueRecipients = all.stream()
                .map(Notification::getRecipientUserId)
                .filter(r -> r != null && !r.trim().isEmpty())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
            response.put("uniqueRecipients", uniqueRecipients);
            response.put("uniqueRecipientCount", uniqueRecipients.size());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            e.printStackTrace();
        }
        return response;
    }
    
    // Test endpoint to create a test notification
    @PostMapping("/debug/test-create")
    @ResponseBody
    public Map<String, Object> testCreateNotification(
            @RequestParam String recipientUserId,
            @RequestParam(required = false, defaultValue = "System") String senderUserId,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            String currentUserId = (String) session.getAttribute("userId");
            if (currentUserId == null) {
                response.put("success", false);
                response.put("error", "Not logged in");
                return response;
            }
            
            System.out.println("=== TEST CREATING NOTIFICATION ===");
            System.out.println("Recipient: '" + recipientUserId + "'");
            System.out.println("Sender: '" + senderUserId + "'");
            System.out.println("Current logged in user: '" + currentUserId + "'");
            
            Notification testNotification = new Notification();
            testNotification.setRecipientUserId(recipientUserId.trim());
            testNotification.setSenderUserId(senderUserId != null ? senderUserId.trim() : currentUserId);
            testNotification.setSenderName("Test System");
            testNotification.setTitle("Test Notification");
            testNotification.setMessage("This is a test notification created for debugging purposes.");
            testNotification.setNotificationType("TEST");
            testNotification.setActionUrl("/api/notifications");
            
            Notification saved = notificationService.createNotification(testNotification);
            
            System.out.println("Test notification created with ID: " + saved.getId());
            System.out.println("RecipientUserId in saved notification: '" + saved.getRecipientUserId() + "'");
            
            response.put("success", true);
            response.put("message", "Test notification created successfully");
            response.put("notificationId", saved.getId());
            response.put("recipientUserId", saved.getRecipientUserId());
        } catch (Exception e) {
            System.err.println("Error creating test notification: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
    
    // Quick cleanup endpoint - DELETE all notifications with empty recipientUserId
    @GetMapping("/debug/cleanup")
    public String cleanupEmptyNotifications(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/loginform";
        }
        
        try {
            List<Notification> all = notificationService.getAllNotifications();
            int deleted = 0;
            for (Notification n : all) {
                String recipient = n.getRecipientUserId();
                if (recipient == null || recipient.trim().isEmpty()) {
                    notificationService.deleteNotification(n.getId());
                    deleted++;
                }
            }
            System.out.println("Cleaned up " + deleted + " notifications with empty recipientUserId");
            return "redirect:/api/notifications?cleanup=" + deleted;
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/api/notifications?error=" + e.getMessage();
        }
    }
    
    // Fix endpoint to delete notifications with empty recipientUserId (they're useless)
    @PostMapping("/debug/delete-empty-recipients")
    @ResponseBody
    public Map<String, Object> deleteEmptyRecipients(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        System.out.println("deleteEmptyRecipients called by userId: " + userId + ", role: " + role);
        
        // Allow ADMIN or any logged-in user to clean up (since these are broken anyway)
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Login required");
            return response;
        }
        
        try {
            List<Notification> all = notificationService.getAllNotifications();
            System.out.println("Total notifications in database: " + all.size());
            
            int deleted = 0;
            List<Long> deletedIds = new java.util.ArrayList<>();
            List<Map<String, Object>> deletedDetails = new java.util.ArrayList<>();
            
            for (Notification n : all) {
                String recipient = n.getRecipientUserId();
                if (recipient == null || recipient.trim().isEmpty()) {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("id", n.getId());
                    detail.put("title", n.getTitle());
                    detail.put("type", n.getNotificationType());
                    detail.put("createdAt", n.getCreatedAt());
                    deletedDetails.add(detail);
                    
                    System.out.println("Deleting notification ID: " + n.getId() + 
                                      " (empty recipientUserId, title: " + n.getTitle() + ")");
                    notificationService.deleteNotification(n.getId());
                    deletedIds.add(n.getId());
                    deleted++;
                }
            }
            
            System.out.println("Deleted " + deleted + " notifications with empty recipientUserId");
            
            response.put("success", true);
            response.put("message", "Deleted " + deleted + " notifications with empty recipientUserId");
            response.put("deletedCount", deleted);
            response.put("deletedIds", deletedIds);
            response.put("deletedDetails", deletedDetails);
            response.put("remainingCount", all.size() - deleted);
        } catch (Exception e) {
            System.err.println("Error deleting empty recipients: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}




