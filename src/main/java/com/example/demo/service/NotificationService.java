package com.example.demo.service;

import com.example.demo.entity.Notification;
import com.example.demo.entity.Ticket;
import com.example.demo.entity.UserAccount;
import com.example.demo.repo.NotificationRepository;
import com.example.demo.repo.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final UserAccountRepository userAccountRepository;

    // Create a notification
    public Notification createNotification(Notification notification) {
        System.out.println("=== createNotification called ===");
        System.out.println("Before save - recipientUserId: '" + notification.getRecipientUserId() + "'");
        System.out.println("Before save - recipientUserId length: " + (notification.getRecipientUserId() != null ? notification.getRecipientUserId().length() : 0));
        System.out.println("Before save - senderUserId: '" + notification.getSenderUserId() + "'");
        System.out.println("Before save - title: '" + notification.getTitle() + "'");
        System.out.println("Before save - type: '" + notification.getNotificationType() + "'");
        
        if (notification.getRecipientUserId() == null || notification.getRecipientUserId().trim().isEmpty()) {
            System.err.println("ERROR: Attempting to create notification with empty recipientUserId!");
            throw new IllegalArgumentException("Recipient userId cannot be null or empty when creating notification");
        }
        
        // Ensure recipientUserId is trimmed and not empty
        String recipientUserId = notification.getRecipientUserId().trim();
        if (recipientUserId.isEmpty()) {
            System.err.println("ERROR: recipientUserId is empty after trim!");
            throw new IllegalArgumentException("Recipient userId cannot be empty after trimming");
        }
        notification.setRecipientUserId(recipientUserId);
        
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        
        // Verify one more time before save
        System.out.println("Right before save - recipientUserId: '" + notification.getRecipientUserId() + "'");
        System.out.println("Right before save - recipientUserId is null: " + (notification.getRecipientUserId() == null));
        System.out.println("Right before save - recipientUserId isEmpty: " + (notification.getRecipientUserId() != null && notification.getRecipientUserId().isEmpty()));
        
        Notification saved = notificationRepository.saveAndFlush(notification);
        
        System.out.println("After save - recipientUserId: '" + saved.getRecipientUserId() + "'");
        System.out.println("After save - recipientUserId length: " + (saved.getRecipientUserId() != null ? saved.getRecipientUserId().length() : 0));
        System.out.println("After save - ID: " + saved.getId());
        
        scheduleEmailNotification(saved);
        
        System.out.println("=================================");
        
        return saved;
    }

    /** Saves notification for in-app bell only — no email (reassign, resolved, status changes). */
    public Notification createInAppNotificationOnly(Notification notification) {
        if (notification.getRecipientUserId() == null || notification.getRecipientUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient userId cannot be null or empty when creating notification");
        }
        notification.setRecipientUserId(notification.getRecipientUserId().trim());
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }
        notification.setIsRead(false);
        return notificationRepository.saveAndFlush(notification);
    }

    /**
     * Create notification and send assignee email immediately (used when a ticket is created).
     */
    @Transactional
    public Notification notifyAssigneeOnTicketCreated(
            Ticket ticket,
            String assigneeUserId,
            String createdByUserId,
            String createdByName,
            String assigneeLevel) {

        if (assigneeUserId == null || assigneeUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("Assignee userId cannot be null or empty");
        }

        String normalizedRecipient = assigneeUserId.trim().toLowerCase();
        Optional<String> recipientEmail = resolveRecipientEmail(normalizedRecipient, ticket.getCompanyCode());
        if (recipientEmail.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot notify assignee on ticket create: no email found for '" + assigneeUserId + "'");
        }

        String normalizedCreator = createdByUserId != null ? createdByUserId.trim().toLowerCase() : "system";

        Notification notification = new Notification();
        notification.setRecipientUserId(normalizedRecipient);
        notification.setSenderUserId(normalizedCreator);
        notification.setSenderName(createdByName != null ? createdByName : "System");
        notification.setTitle("New Ticket Created — " + (ticket.getTicketNo() != null ? ticket.getTicketNo() : ""));
        notification.setMessage("A new ticket has been created and you are assigned as " + assigneeLevel + ". "
                + (ticket.getIssueDescription() != null ? ticket.getIssueDescription() : ""));
        notification.setNotificationType("TICKET_CREATED");
        notification.setReferenceId(ticket.getId());
        notification.setReferenceType("TICKET");
        notification.setReferenceCode(ticket.getTicketNo());
        notification.setActionUrl("/api/tickets/view/" + ticket.getId());
        notification.setCompanyCode(ticket.getCompanyCode());
        notification.setPriority(getPriorityScore(ticket.getPriority()));
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);

        Notification saved = notificationRepository.saveAndFlush(notification);
        scheduleEmailNotification(saved);
        return saved;
    }

    private void scheduleEmailNotification(Notification saved) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                sendEmailNotification(saved);
            } catch (Exception e) {
                System.err.println("Async email send error: " + e.getMessage());
            }
        });
    }

    /**
     * Resolve assignee identifier (email, userId, or display name) to a deliverable email address.
     */
    public Optional<String> resolveRecipientEmail(String assigneeIdentifier, String companyCode) {
        if (assigneeIdentifier == null || assigneeIdentifier.trim().isEmpty()) {
            return Optional.empty();
        }
        String trimmed = assigneeIdentifier.trim();
        if (trimmed.contains("@") && trimmed.contains(".")) {
            return Optional.of(trimmed.toLowerCase());
        }

        String searchKey = trimmed.toLowerCase();
        UserAccount user = userAccountRepository.findByUserId(searchKey);
        if (user == null) {
            user = userAccountRepository.findByEmail(searchKey).orElse(null);
        }
        if (user == null) {
            user = userAccountRepository.findByFullName(trimmed).orElse(null);
        }
        if (user == null && companyCode != null && !companyCode.isBlank()) {
            for (UserAccount u : userAccountRepository.findByCompanyCode(companyCode)) {
                String fullName = ((u.getFirstName() != null ? u.getFirstName() : "") + " "
                        + (u.getLastName() != null ? u.getLastName() : "")).trim();
                if (fullName.equalsIgnoreCase(trimmed)
                        || (u.getFirstName() != null && u.getFirstName().trim().equalsIgnoreCase(trimmed))
                        || (u.getLastName() != null && u.getLastName().trim().equalsIgnoreCase(trimmed))) {
                    user = u;
                    break;
                }
            }
        }
        if (user == null) {
            List<UserAccount> allUsers = userAccountRepository.findAll();
            for (UserAccount u : allUsers) {
                if (u.getFirstName() != null && u.getFirstName().trim().equalsIgnoreCase(searchKey)) {
                    user = u;
                    break;
                }
                if (u.getLastName() != null && u.getLastName().trim().equalsIgnoreCase(searchKey)) {
                    user = u;
                    break;
                }
                String fullName = ((u.getFirstName() != null ? u.getFirstName() : "") + " "
                        + (u.getLastName() != null ? u.getLastName() : "")).trim();
                if (fullName.equalsIgnoreCase(searchKey)) {
                    user = u;
                    break;
                }
            }
        }
        if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
            return Optional.of(user.getEmail().trim().toLowerCase());
        }
        if (user != null && user.getUserId() != null && user.getUserId().contains("@")) {
            return Optional.of(user.getUserId().trim().toLowerCase());
        }
        return Optional.empty();
    }

    private void sendEmailNotificationMandatory(Notification notification) {
        sendEmailNotification(notification, true);
    }

    void sendEmailNotification(Notification notification) {
        sendEmailNotification(notification, false);
    }

    private void sendEmailNotification(Notification notification, boolean mandatory) {
        try {
            System.out.println("Attemping to send email notification to: " + notification.getRecipientUserId());

            Optional<String> emailOpt = resolveRecipientEmail(
                    notification.getRecipientUserId(),
                    notification.getCompanyCode());
            if (emailOpt.isEmpty()) {
                String msg = "Could not find email for recipientUserId: " + notification.getRecipientUserId();
                System.err.println(msg);
                if (mandatory) {
                    throw new IllegalStateException(msg);
                }
                return;
            }
            String recipientEmail = emailOpt.get();

            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("info@iwpl.org", "PerfectQA Portal");
            helper.setTo(recipientEmail);
            
            String subject;
            String htmlContent;
            
            if ("TICKET_ASSIGNED".equals(notification.getNotificationType())
                    || "TICKET_CREATED".equals(notification.getNotificationType())) {
                String ticketNo = notification.getReferenceCode() != null ? notification.getReferenceCode() : "";
                subject = "TICKET_CREATED".equals(notification.getNotificationType())
                        ? "New ticket assigned to you: " + ticketNo
                        : "You received a ticket: " + ticketNo;
                String heading = "TICKET_CREATED".equals(notification.getNotificationType())
                        ? "A new ticket has been created for you!"
                        : "You received a ticket!";
                htmlContent = String.format(
                    "<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head>" +
                    "<body style='font-family: Inter, Arial, sans-serif; background-color: #f4f7f9; margin: 0; padding: 20px;'>" +
                    "<div style='max-width: 600px; width: 100%%; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.05); border: 1px solid #e2e8f0;'>" +
                    "<div style='background-color: #4f46e5; color: #ffffff; padding: 30px 20px; text-align: center;'>" +
                    "<h1 style='margin: 0; font-size: 24px; font-weight: 700; letter-spacing: -0.025em;'>PerfectQA Portal</h1>" +
                    "<p style='margin: 5px 0 0; opacity: 0.8; font-size: 14px;'>Ticketing & Support System</p>" +
                    "</div>" +
                    "<div style='padding: 30px 20px; text-align: center;'>" +
                    "<h2 style='color: #1e293b; font-size: 22px; margin-top: 0; margin-bottom: 10px; word-wrap: break-word;'>%s</h2>" +
                    "<p style='font-size: 15px; color: #475569; line-height: 1.5; margin-bottom: 25px;'>Please review and action this ticket in PerfectQA.</p>" +
                    
                    "<div style='background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; text-align: left; margin: 25px 0; overflow-x: auto;'>" +
                    "<table style='width: 100%%; border-collapse: collapse; min-width: 250px;'>" +
                    "<tr>" +
                    "<td style='padding: 8px 0; font-size: 14px; color: #64748b; font-weight: 600; width: 35%%; vertical-align: top;'>Ticket No:</td>" +
                    "<td style='padding: 8px 0; font-size: 14px; color: #4f46e5; font-weight: 700; vertical-align: top; word-break: break-word;'>%s</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='padding: 8px 0; font-size: 14px; color: #64748b; font-weight: 600; vertical-align: top;'>Description:</td>" +
                    "<td style='padding: 8px 0; font-size: 14px; color: #0f172a; line-height: 1.5; vertical-align: top; word-break: break-word;'>%s</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='padding: 8px 0; font-size: 14px; color: #64748b; font-weight: 600; vertical-align: top;'>Sender:</td>" +
                    "<td style='padding: 8px 0; font-size: 14px; color: #0f172a; font-weight: 600; vertical-align: top; word-break: break-word;'>%s</td>" +
                    "</tr>" +
                    "</table>" +
                    "</div>" +
                    
                    "<div style='margin: 35px 0;'>" +
                    "<a href='http://perfectqa.net%s' style='background-color: #4f46e5; color: #ffffff; padding: 14px 28px; border-radius: 10px; text-decoration: none; font-weight: 700; font-size: 15px; display: inline-block; box-shadow: 0 4px 6px -1px rgba(79, 70, 229, 0.2);'>View Ticket Details</a>" +
                    "</div>" +
                    "</div>" +
                    "<div style='background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #e2e8f0;'>" +
                    "<p style='margin: 0; font-size: 12px; color: #94a3b8;'>&copy; 2026 PerfectQA - Intelligent Testing Platform.<br>IWPL Inc.</p>" +
                    "</div>" +
                    "</div>" +
                    "</body></html>",
                    heading,
                    ticketNo.isEmpty() ? "N/A" : ticketNo,
                    notification.getMessage() != null ? notification.getMessage() : "",
                    notification.getSenderName() != null ? notification.getSenderName() : "System",
                    notification.getActionUrl() != null ? notification.getActionUrl() : "/"
                );
            } else {
                subject = "PerfectQA: " + notification.getTitle();
                htmlContent = String.format(
                    "<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head>" +
                    "<body style='font-family: Inter, Arial, sans-serif; background-color: #f4f7f9; margin: 0; padding: 20px;'>" +
                    "<div style='max-width: 600px; width: 100%%; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.05); border: 1px solid #e2e8f0;'>" +
                    "<div style='background-color: #0f172a; color: #ffffff; padding: 30px 20px; text-align: center;'>" +
                    "<h1 style='margin: 0; font-size: 24px; font-weight: 700; letter-spacing: -0.025em;'>PerfectQA</h1>" +
                    "<p style='margin: 5px 0 0; opacity: 0.8; font-size: 14px;'>Intelligent Testing Platform</p>" +
                    "</div>" +
                    "<div style='padding: 30px 20px; text-align: center;'>" +
                    "<h2 style='color: #1e293b; font-size: 20px; margin-top: 0; margin-bottom: 20px; word-wrap: break-word;'>%s</h2>" +
                    "<p style='font-size: 15px; color: #475569; line-height: 1.6; margin-bottom: 25px;'>%s</p>" +
                    
                    "<div style='background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; text-align: left; margin: 25px 0; overflow-x: auto;'>" +
                    "<table style='width: 100%%; border-collapse: collapse; min-width: 250px;'>" +
                    "<tr>" +
                    "<td style='padding: 6px 0; font-size: 14px; color: #64748b; font-weight: 600; width: 35%%; vertical-align: top;'>Type:</td>" +
                    "<td style='padding: 6px 0; font-size: 14px; color: #0f172a; vertical-align: top; word-break: break-word;'>%s</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='padding: 6px 0; font-size: 14px; color: #64748b; font-weight: 600; vertical-align: top;'>Reference:</td>" +
                    "<td style='padding: 6px 0; font-size: 14px; color: #0f172a; vertical-align: top; word-break: break-word;'>%s</td>" +
                    "</tr>" +
                    "<tr>" +
                    "<td style='padding: 6px 0; font-size: 14px; color: #64748b; font-weight: 600; vertical-align: top;'>Sender:</td>" +
                    "<td style='padding: 6px 0; font-size: 14px; color: #0f172a; vertical-align: top; word-break: break-word;'>%s</td>" +
                    "</tr>" +
                    "</table>" +
                    "</div>" +
                    
                    "<div style='margin: 35px 0;'>" +
                    "<a href='http://perfectqa.net%s' style='background-color: #0f172a; color: #ffffff; padding: 14px 28px; border-radius: 10px; text-decoration: none; font-weight: 700; font-size: 15px; display: inline-block;'>View Platform Details</a>" +
                    "</div>" +
                    "</div>" +
                    "<div style='background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #e2e8f0;'>" +
                    "<p style='margin: 0; font-size: 12px; color: #94a3b8;'>&copy; 2026 PerfectQA - Intelligent Testing Platform.<br>IWPL Inc.</p>" +
                    "</div>" +
                    "</div>" +
                    "</body></html>",
                    notification.getTitle(),
                    notification.getMessage(),
                    notification.getNotificationType(),
                    notification.getReferenceCode() != null ? notification.getReferenceCode() : "N/A",
                    notification.getSenderName() != null ? notification.getSenderName() : "System",
                    notification.getActionUrl() != null ? notification.getActionUrl() : "/"
                );
            }
            
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("HTML Email notification sent successfully to: " + recipientEmail);
            
        } catch (Exception e) {
            System.err.println("FAILED to send email notification: " + e.getMessage());
            e.printStackTrace();
            if (mandatory) {
                throw new IllegalStateException("Failed to send ticket creation email: " + e.getMessage(), e);
            }
        }
    }

    // Create a ticket assignment notification
    public Notification createTicketAssignmentNotification(
            Ticket ticket,
            String assignedToUserId,
            String assignedByUserId,
            String assignedByName) {
        
        // CRITICAL: Normalize userIds to match session format exactly
        // Session stores userId as email (lowercase, trimmed)
        // This ensures recipientUserId matches session.getAttribute("userId") exactly
        assignedToUserId = assignedToUserId != null ? assignedToUserId.trim().toLowerCase() : null;
        assignedByUserId = assignedByUserId != null ? assignedByUserId.trim().toLowerCase() : null;
        
        System.out.println("=== NotificationService.createTicketAssignmentNotification ===");
        System.out.println("Recipient userId (assignedTo) - NORMALIZED: '" + assignedToUserId + "'");
        System.out.println("Sender userId (assignedBy) - NORMALIZED: '" + assignedByUserId + "'");
        System.out.println("Sender name: '" + assignedByName + "'");
        System.out.println("Ticket No: " + ticket.getTicketNo());
        System.out.println("Ticket ID: " + ticket.getId());
        
        if (assignedToUserId == null || assignedToUserId.isEmpty()) {
            throw new IllegalArgumentException("Recipient userId cannot be null or empty");
        }
        
        // Double-check that assignedToUserId is not null or empty after normalization
        if (assignedToUserId == null || assignedToUserId.trim().isEmpty()) {
            System.err.println("ERROR: assignedToUserId is null or empty after normalization! Cannot create notification.");
            throw new IllegalArgumentException("Recipient userId (assignedToUserId) cannot be null or empty");
        }
        
        Notification notification = new Notification();
        notification.setRecipientUserId(assignedToUserId); // Normalized to lowercase to match session userId
        notification.setSenderUserId(assignedByUserId);
        notification.setSenderName(assignedByName);
        notification.setTitle("New Ticket Assigned");
        
        String message;
        if (assignedToUserId.equalsIgnoreCase(ticket.getAssigneeUserCode()) 
                || assignedToUserId.equalsIgnoreCase(ticket.getAssignee2UserCode()) 
                || assignedToUserId.equalsIgnoreCase(ticket.getAssignee3UserCode())) {
            message = "You have been assigned to ticket " + ticket.getTicketNo() + 
                    (ticket.getIssueDescription() != null ? ": " + ticket.getIssueDescription() : "");
        } else {
            String assigneeDisplay = ticket.getAssigneeName() != null ? ticket.getAssigneeName() : ticket.getAssigneeUserCode();
            if (assigneeDisplay == null) assigneeDisplay = ticket.getAssignedTo();
            if (assigneeDisplay == null) assigneeDisplay = "someone";
            message = "Ticket " + ticket.getTicketNo() + " has been assigned to " + assigneeDisplay + 
                    (ticket.getIssueDescription() != null ? ": " + ticket.getIssueDescription() : "");
        }
        notification.setMessage(message);
        
        notification.setNotificationType("TICKET_ASSIGNED");
        notification.setReferenceId(ticket.getId());
        notification.setReferenceType("TICKET");
        notification.setReferenceCode(ticket.getTicketNo());
        notification.setActionUrl("/api/tickets/view/" + ticket.getId());
        notification.setCompanyCode(ticket.getCompanyCode());
        notification.setPriority(getPriorityScore(ticket.getPriority()));
        
        // Verify before saving
        System.out.println("About to save notification with recipientUserId: '" + notification.getRecipientUserId() + "'");
        if (notification.getRecipientUserId() == null || notification.getRecipientUserId().trim().isEmpty()) {
            System.err.println("ERROR: recipientUserId is null or empty right before save!");
            throw new IllegalStateException("recipientUserId cannot be null or empty");
        }
        
        Notification saved = createNotification(notification);
        System.out.println("Notification saved with ID: " + saved.getId());
        System.out.println("Saved recipientUserId: '" + saved.getRecipientUserId() + "'");
        
        // Verify after saving
        if (saved.getRecipientUserId() == null || saved.getRecipientUserId().trim().isEmpty()) {
            System.err.println("ERROR: recipientUserId became empty after save! This is a database issue.");
        }
        
        System.out.println("================================================");
        
        return saved;
    }

    // Create a ticket status change notification
    public Notification createTicketStatusChangeNotification(
            Ticket ticket,
            String recipientUserId,
            String changedByUserId,
            String changedByName,
            String oldStatus,
            String newStatus) {
        
        Notification notification = new Notification();
        notification.setRecipientUserId(recipientUserId);
        notification.setSenderUserId(changedByUserId);
        notification.setSenderName(changedByName);
        notification.setTitle("Ticket Status Updated");
        notification.setMessage("Ticket " + ticket.getTicketNo() + " status changed from " + oldStatus + " to " + newStatus);
        notification.setNotificationType("TICKET_STATUS_CHANGED");
        notification.setReferenceId(ticket.getId());
        notification.setReferenceType("TICKET");
        notification.setReferenceCode(ticket.getTicketNo());
        notification.setActionUrl("/api/tickets/view/" + ticket.getId());
        notification.setCompanyCode(ticket.getCompanyCode());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);

        return createNotification(notification);
    }

    // Create a ticket update notification
    public Notification createTicketUpdateNotification(
            Ticket ticket,
            String recipientUserId,
            String updatedByUserId,
            String updatedByName,
            String changeDetails) {
        
        Notification notification = new Notification();
        notification.setRecipientUserId(recipientUserId);
        notification.setSenderUserId(updatedByUserId);
        notification.setSenderName(updatedByName);
        notification.setTitle("Ticket Details Updated");
        notification.setMessage("Ticket " + ticket.getTicketNo() + " was updated by " + updatedByName + ". Changes: " + changeDetails);
        notification.setNotificationType("TICKET_UPDATED");
        notification.setReferenceId(ticket.getId());
        notification.setReferenceType("TICKET");
        notification.setReferenceCode(ticket.getTicketNo());
        notification.setActionUrl("/api/tickets/view/" + ticket.getId());
        notification.setCompanyCode(ticket.getCompanyCode());
        
        return createNotification(notification);
    }

    // Create a ticket comment notification
    public Notification createTicketCommentNotification(
            Ticket ticket,
            String recipientUserId,
            String commentedByUserId,
            String commentedByName,
            String commentPreview) {
        
        Notification notification = new Notification();
        notification.setRecipientUserId(recipientUserId);
        notification.setSenderUserId(commentedByUserId);
        notification.setSenderName(commentedByName);
        notification.setTitle("New Comment on Ticket");
        notification.setMessage(commentedByName + " commented on ticket " + ticket.getTicketNo() + 
                (commentPreview != null ? ": \"" + commentPreview + "\"" : ""));
        notification.setNotificationType("TICKET_COMMENT");
        notification.setReferenceId(ticket.getId());
        notification.setReferenceType("TICKET");
        notification.setReferenceCode(ticket.getTicketNo());
        notification.setActionUrl("/api/tickets/view/" + ticket.getId());
        notification.setCompanyCode(ticket.getCompanyCode());
        
        return createNotification(notification);
    }

    // Create a task edit notification
    public Notification createTicketTaskEditNotification(
            Ticket ticket,
            String recipientUserId,
            String editedByUserId,
            String editedByName,
            String taskType) {
        
        Notification notification = new Notification();
        notification.setRecipientUserId(recipientUserId);
        notification.setSenderUserId(editedByUserId);
        notification.setSenderName(editedByName);
        notification.setTitle("Closed Task Edited");
        notification.setMessage("A closed task (" + taskType + ") on ticket " + ticket.getTicketNo() + " was edited by " + editedByName);
        notification.setNotificationType("TASK_EDITED");
        notification.setReferenceId(ticket.getId());
        notification.setReferenceType("TICKET");
        notification.setReferenceCode(ticket.getTicketNo());
        notification.setActionUrl("/api/tickets/view/" + ticket.getId());
        notification.setCompanyCode(ticket.getCompanyCode());
        
        return createNotification(notification);
    }

    // Get all notifications for a user
    public List<Notification> getNotificationsForUser(String userId) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId);
    }
    
    // Get all notifications (for debugging)
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    // Get unread notifications for a user
    public List<Notification> getUnreadNotificationsForUser(String userId) {
        if (userId == null) {
            System.out.println("getUnreadNotificationsForUser: userId is null");
            return List.of();
        }
        
        // CRITICAL: Normalize userId to lowercase for exact match
        String normalizedUserId = userId.trim().toLowerCase();
        System.out.println("getUnreadNotificationsForUser: Searching for normalized userId '" + normalizedUserId + "' (original: '" + userId + "')");
        
        // Debug: Check all notifications in database to see what recipientUserIds exist
        List<Notification> allNotifications = notificationRepository.findAll();
        System.out.println("=== DEBUG: All notifications in database ===");
        System.out.println("Total notifications in DB: " + allNotifications.size());
        for (Notification n : allNotifications) {
            System.out.println("  Notification ID: " + n.getId() + 
                             ", Recipient: '" + n.getRecipientUserId() + 
                             "', Type: " + n.getNotificationType() + 
                             ", Read: " + n.getIsRead());
        }
        System.out.println("===========================================");
        
        List<Notification> notifications = notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(normalizedUserId);
        System.out.println("Found " + notifications.size() + " unread notifications for normalized userId '" + normalizedUserId + "'");
        
        // Debug: Show all recipient user IDs in database for this user (case-insensitive check)
        if (notifications.isEmpty()) {
            List<Notification> allUserNotifications = notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(normalizedUserId);
            System.out.println("Total notifications (including read) for normalized userId '" + normalizedUserId + "': " + allUserNotifications.size());
            
            // Try case-insensitive search for debugging
            System.out.println("Checking for case mismatch...");
            for (Notification n : allNotifications) {
                if (n.getRecipientUserId() != null && n.getRecipientUserId().equalsIgnoreCase(normalizedUserId) && !n.getRecipientUserId().equals(normalizedUserId)) {
                    System.out.println("  ⚠️ CASE MISMATCH FOUND: ID=" + n.getId() + 
                                     ", stored recipientUserId='" + n.getRecipientUserId() + 
                                     "', searching for='" + normalizedUserId + "'");
                }
            }
        }
        
        return notifications;
    }

    // Count unread notifications
    public long countUnreadNotifications(String userId) {
        if (userId == null) {
            System.out.println("countUnreadNotifications: userId is null");
            return 0;
        }
        
        // CRITICAL: Normalize userId to lowercase for exact match
        String normalizedUserId = userId.trim().toLowerCase();
        System.out.println("countUnreadNotifications: Searching for normalized userId '" + normalizedUserId + "' (original: '" + userId + "')");
        
        // Debug: List all notifications to see what's in the database
        List<Notification> all = notificationRepository.findAll();
        System.out.println("=== DEBUG: All notifications in database (countUnreadNotifications) ===");
        System.out.println("Total notifications in DB: " + all.size());
        for (Notification n : all) {
            System.out.println("  ID: " + n.getId() + 
                             ", Recipient: '" + n.getRecipientUserId() + 
                             "', Searching for: '" + userId + 
                             "', Match: " + (userId.equals(n.getRecipientUserId())) +
                             ", Type: " + n.getNotificationType());
        }
        System.out.println("===========================================");
        
        long count = notificationRepository.countUnreadByUserId(normalizedUserId);
        System.out.println("Unread count for normalized userId '" + normalizedUserId + "': " + count);
        return count;
    }

    // Mark notification as read
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    // Mark all notifications as read for a user
    @Transactional
    public int markAllAsRead(String userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    // Get notification by ID
    public Notification getById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    // Delete notification
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    // Delete all read notifications older than specified days
    @Transactional
    public int cleanupOldNotifications(int daysOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysOld);
        return notificationRepository.deleteOldReadNotifications(cutoff);
    }

    // Helper to convert priority string to score
    private int getPriorityScore(String priority) {
        if (priority == null) return 0;
        return switch (priority.toUpperCase()) {
            case "HIGH", "CRITICAL" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }
}




