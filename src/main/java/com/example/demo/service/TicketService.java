package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Notification;
import com.example.demo.entity.Ticket;
import com.example.demo.entity.TicketComment;
import com.example.demo.entity.TicketHistory;
import com.example.demo.entity.UserAccount;
import com.example.demo.repo.TicketRepository;
import com.example.demo.repo.TicketCommentRepository;
import com.example.demo.repo.TicketHistoryRepository;
import com.example.demo.repo.UserAccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final NotificationService notificationService;
    private final UserAccountRepository userAccountRepository;

    /** Set when ticket is saved but assignee creation email fails (shown once in UI). */
    private String lastCreateEmailWarning;

    // Generate Ticket Number
    public String generateTicketNo() {
        long count = ticketRepository.count() + 1;
        return "TKT-" + String.format("%05d", count);
    }

    // Auto-set priority based on issue type
    private String calculatePriority(String issueType) {
        if (issueType == null) return "MEDIUM";
        return switch (issueType.toLowerCase()) {
            case "critical" -> "HIGH";
            case "major" -> "MEDIUM";
            default -> "LOW";
        };
    }

    public String consumeLastCreateEmailWarning() {
        String warning = lastCreateEmailWarning;
        lastCreateEmailWarning = null;
        return warning;
    }

    // Create a ticket
    public Ticket createTicket(Ticket ticket, String username) {
        lastCreateEmailWarning = null;
        if (ticket.getTicketNo() == null || ticket.getTicketNo().trim().isEmpty()) {
            ticket.setTicketNo(generateTicketNo());
        }
        // Only auto-calculate priority if user didn't set one
        if (ticket.getPriority() == null || ticket.getPriority().trim().isEmpty()) {
            ticket.setPriority(calculatePriority(ticket.getIssueType()));
        }
        ticket.setDatetime(LocalDateTime.now());
        ticket.setCreatedDate(LocalDate.now());
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setCreatedBy(username);
        ticket.setStatus("OPEN");
        
        // Auto-assign department from creator
        UserAccount creator = userAccountRepository.findByUserId(username.toLowerCase());
        if (creator != null) {
            ticket.setDepartmentCode(creator.getDepartment());
        }
        
        // Resolve all assignee user codes to actual user IDs/emails before saving
        if (ticket.getAssigneeUserCode() != null && !ticket.getAssigneeUserCode().trim().isEmpty()) {
            String resolved = resolveAssigneeToUserId(ticket.getAssigneeUserCode().trim(), ticket.getCompanyCode());
            if (resolved != null) {
                ticket.setAssigneeUserCode(resolved);
            }
        }
        if (ticket.getAssignee2UserCode() != null && !ticket.getAssignee2UserCode().trim().isEmpty()) {
            String resolved = resolveAssigneeToUserId(ticket.getAssignee2UserCode().trim(), ticket.getCompanyCode());
            if (resolved != null) {
                ticket.setAssignee2UserCode(resolved);
            }
        }
        if (ticket.getAssignee3UserCode() != null && !ticket.getAssignee3UserCode().trim().isEmpty()) {
            String resolved = resolveAssigneeToUserId(ticket.getAssignee3UserCode().trim(), ticket.getCompanyCode());
            if (resolved != null) {
                ticket.setAssignee3UserCode(resolved);
            }
        }

        // Populate assignee name for the primary assignee
        updateAssigneeName(ticket);

        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Add history entry
        addHistoryEntry(savedTicket, "CREATED", null, "OPEN", "Ticket created", username);

        String assignedByUserId = (username != null ? username.trim().toLowerCase() : "system");
        String assignedByName = (username != null ? username : "System");
        
        // Notify Primary Assignee (Assignee 1)
        if (savedTicket.getAssigneeUserCode() != null && !savedTicket.getAssigneeUserCode().trim().isEmpty()) {
            try {
                String assigneeInput = savedTicket.getAssigneeUserCode().trim();
                String assigneeUserId = resolveAssigneeToUserId(assigneeInput, savedTicket.getCompanyCode());
                if (assigneeUserId == null) {
                    assigneeUserId = assigneeInput;
                }
                
                System.out.println("=== CREATING NOTIFICATION FOR PRIMARY ASSIGNEE ===");
                System.out.println("Ticket ID: " + savedTicket.getId());
                System.out.println("Assignee Input: " + assigneeInput);
                System.out.println("Resolved UserId: " + assigneeUserId);
                
                Notification notification = notifyAssigneeOnTicketCreated(
                    savedTicket,
                    assigneeUserId,
                    assignedByUserId,
                    assignedByName,
                    "Primary Assignee"
                );
                
                System.out.println("Primary assignee notification created! ID: " + notification.getId());
            } catch (Exception e) {
                System.err.println("=== FAILED TO EMAIL PRIMARY ASSIGNEE ON TICKET CREATE ===");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
                lastCreateEmailWarning = "Ticket saved, but email to primary assignee failed: " + e.getMessage();
            }
        }
        
        // Notify Secondary Assignee (Assignee 2)
        if (savedTicket.getAssignee2UserCode() != null && !savedTicket.getAssignee2UserCode().trim().isEmpty()) {
            try {
                String assigneeInput = savedTicket.getAssignee2UserCode().trim();
                String assigneeUserId = resolveAssigneeToUserId(assigneeInput, savedTicket.getCompanyCode());
                if (assigneeUserId == null) {
                    assigneeUserId = assigneeInput;
                }
                
                System.out.println("=== CREATING NOTIFICATION FOR SECONDARY ASSIGNEE ===");
                System.out.println("Ticket ID: " + savedTicket.getId());
                System.out.println("Assignee Input: " + assigneeInput);
                System.out.println("Resolved UserId: " + assigneeUserId);
                
                Notification notification = notifyAssigneeOnTicketCreated(
                    savedTicket,
                    assigneeUserId,
                    assignedByUserId,
                    assignedByName,
                    "Secondary Assignee"
                );
                
                System.out.println("Secondary assignee notification created! ID: " + notification.getId());
            } catch (Exception e) {
                System.err.println("=== FAILED TO CREATE NOTIFICATION FOR SECONDARY ASSIGNEE ===");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Notify 3rd Assignee (Assignee 3)
        if (savedTicket.getAssignee3UserCode() != null && !savedTicket.getAssignee3UserCode().trim().isEmpty()) {
            try {
                String assigneeInput = savedTicket.getAssignee3UserCode().trim();
                String assigneeUserId = resolveAssigneeToUserId(assigneeInput, savedTicket.getCompanyCode());
                if (assigneeUserId == null) {
                    assigneeUserId = assigneeInput;
                }
                
                System.out.println("=== CREATING NOTIFICATION FOR 3RD ASSIGNEE ===");
                System.out.println("Ticket ID: " + savedTicket.getId());
                System.out.println("Assignee Input: " + assigneeInput);
                System.out.println("Resolved UserId: " + assigneeUserId);
                
                Notification notification = notifyAssigneeOnTicketCreated(
                    savedTicket,
                    assigneeUserId,
                    assignedByUserId,
                    assignedByName,
                    "3rd Assignee"
                );
                
                System.out.println("3rd assignee notification created! ID: " + notification.getId());
            } catch (Exception e) {
                System.err.println("=== FAILED TO CREATE NOTIFICATION FOR 3RD ASSIGNEE ===");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Legacy: If ticket is assigned during creation via assignedTo field, create notification
        if (savedTicket.getAssignedTo() != null && !savedTicket.getAssignedTo().trim().isEmpty() 
            && savedTicket.getAssigneeUserCode() == null) {
            try {
                String assignedToInput = savedTicket.getAssignedTo().trim();
                String assignedToUserId = resolveAssigneeToUserId(assignedToInput, savedTicket.getCompanyCode());
                if (assignedToUserId == null) {
                    assignedToUserId = assignedToInput;
                }
                
                System.out.println("=== CREATING NOTIFICATION FOR NEW TICKET (Legacy assignedTo) ===");
                System.out.println("Ticket ID: " + savedTicket.getId());
                System.out.println("Assignee Input: " + assignedToInput);
                System.out.println("Resolved UserId: " + assignedToUserId);
                
                Notification notification = notifyAssigneeOnTicketCreated(
                    savedTicket,
                    assignedToUserId,
                    assignedByUserId,
                    assignedByName,
                    "Primary Assignee"
                );
                
                System.out.println("Ticket-created email notification sent! ID: " + notification.getId());
            } catch (Exception e) {
                System.err.println("=== FAILED TO CREATE NOTIFICATION FOR NEW TICKET ===");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return savedTicket;
    }

    // Update Ticket
    public Ticket updateTicket(Long id, Ticket updated, String username) {
        Optional<Ticket> existing = ticketRepository.findById(id);

        if (existing.isEmpty()) {
            throw new RuntimeException("Ticket Not Found");
        }

        Ticket ticket = existing.get();
        
        // Track changes for history
        StringBuilder changes = new StringBuilder();

        if (updated.getIssueType() != null && !updated.getIssueType().equals(ticket.getIssueType())) {
            changes.append("Issue Type: ").append(ticket.getIssueType()).append(" -> ").append(updated.getIssueType()).append("; ");
            ticket.setIssueType(updated.getIssueType());
            // Only auto-calculate priority if user didn't explicitly set one
            if (updated.getPriority() == null || updated.getPriority().trim().isEmpty()) {
                ticket.setPriority(calculatePriority(updated.getIssueType()));
            }
        }
        
        // Update priority if explicitly provided
        if (updated.getPriority() != null && !updated.getPriority().trim().isEmpty()) {
            if (!updated.getPriority().equals(ticket.getPriority())) {
                changes.append("Priority: ").append(ticket.getPriority()).append(" -> ").append(updated.getPriority()).append("; ");
            }
            ticket.setPriority(updated.getPriority());
        }
        
        if (updated.getModule() != null) {
            ticket.setModule(updated.getModule());
        }
        if (updated.getModuleCode() != null) {
            ticket.setModuleCode(updated.getModuleCode());
        }
        if (updated.getIssueDescription() != null) {
            ticket.setIssueDescription(updated.getIssueDescription());
        }
        if (updated.getIssueLongDescription() != null) {
            ticket.setIssueLongDescription(updated.getIssueLongDescription());
        }
        if (updated.getSummary() != null) {
            ticket.setSummary(updated.getSummary());
        }
        if (updated.getDescription() != null) {
            ticket.setDescription(updated.getDescription());
        }
        if (updated.getStepsToReproduce() != null) {
            ticket.setStepsToReproduce(updated.getStepsToReproduce());
        }
        if (updated.getExpectedResult() != null) {
            ticket.setExpectedResult(updated.getExpectedResult());
        }
        if (updated.getActualResult() != null) {
            ticket.setActualResult(updated.getActualResult());
        }
        if (updated.getErrorCode() != null) {
            ticket.setErrorCode(updated.getErrorCode());
        }
        if (updated.getApplicationCode() != null) {
            ticket.setApplicationCode(updated.getApplicationCode());
        }
        if (updated.getTestCaseCode() != null) {
            ticket.setTestCaseCode(updated.getTestCaseCode());
        }
        if (updated.getAdditionalComments() != null) {
            ticket.setAdditionalComments(updated.getAdditionalComments());
        }
        
        // Track assignee changes and send notifications
        String assignedByUserId = (username != null ? username.trim().toLowerCase() : "system");
        String assignedByName = (username != null ? username : "System");
        
        // Track which assignees were newly added/changed
        String newPrimaryAssignee = null;
        String newSecondaryAssignee = null;
        String newThirdAssignee = null;
        
        // Check for Primary Assignee changes
        if (updated.getAssigneeUserCode() != null && !updated.getAssigneeUserCode().trim().isEmpty()) {
            String newAssignee = updated.getAssigneeUserCode().trim();
            String resolvedNewAssignee = resolveAssigneeToUserId(newAssignee, ticket.getCompanyCode());
            if (resolvedNewAssignee == null) {
                resolvedNewAssignee = newAssignee;
            }
            if (ticket.getAssigneeUserCode() == null || !ticket.getAssigneeUserCode().equalsIgnoreCase(resolvedNewAssignee)) {
                String oldAssignee = ticket.getAssigneeUserCode();
                ticket.setAssigneeUserCode(resolvedNewAssignee);
                changes.append("Primary Assignee: ").append(oldAssignee != null ? oldAssignee : "None").append(" -> ").append(resolvedNewAssignee).append("; ");
                newPrimaryAssignee = resolvedNewAssignee;
                // Update assignee name
                updateAssigneeName(ticket);
            }
        }
        
        // Check for Secondary Assignee changes
        if (updated.getAssignee2UserCode() != null && !updated.getAssignee2UserCode().trim().isEmpty()) {
            String newAssignee = updated.getAssignee2UserCode().trim();
            String resolvedNewAssignee = resolveAssigneeToUserId(newAssignee, ticket.getCompanyCode());
            if (resolvedNewAssignee == null) {
                resolvedNewAssignee = newAssignee;
            }
            if (ticket.getAssignee2UserCode() == null || !ticket.getAssignee2UserCode().equalsIgnoreCase(resolvedNewAssignee)) {
                String oldAssignee = ticket.getAssignee2UserCode();
                ticket.setAssignee2UserCode(resolvedNewAssignee);
                changes.append("Secondary Assignee: ").append(oldAssignee != null ? oldAssignee : "None").append(" -> ").append(resolvedNewAssignee).append("; ");
                newSecondaryAssignee = resolvedNewAssignee;
            }
        }
        
        // Check for 3rd Assignee changes
        if (updated.getAssignee3UserCode() != null && !updated.getAssignee3UserCode().trim().isEmpty()) {
            String newAssignee = updated.getAssignee3UserCode().trim();
            String resolvedNewAssignee = resolveAssigneeToUserId(newAssignee, ticket.getCompanyCode());
            if (resolvedNewAssignee == null) {
                resolvedNewAssignee = newAssignee;
            }
            if (ticket.getAssignee3UserCode() == null || !ticket.getAssignee3UserCode().equalsIgnoreCase(resolvedNewAssignee)) {
                String oldAssignee = ticket.getAssignee3UserCode();
                ticket.setAssignee3UserCode(resolvedNewAssignee);
                changes.append("3rd Assignee: ").append(oldAssignee != null ? oldAssignee : "None").append(" -> ").append(resolvedNewAssignee).append("; ");
                newThirdAssignee = resolvedNewAssignee;
            }
        }

        if (updated.getAttachments() != null && !updated.getAttachments().trim().isEmpty()) {
            String oldAttachments = ticket.getAttachments() != null ? ticket.getAttachments() : ticket.getAttachmentPaths();
            String merged;
            if (oldAttachments == null || oldAttachments.trim().isEmpty()) {
                merged = updated.getAttachments().trim();
            } else {
                merged = oldAttachments.trim() + "," + updated.getAttachments().trim();
            }
            ticket.setAttachments(merged);
            ticket.setAttachmentPaths(merged);
            changes.append("Attachments added: ").append(updated.getAttachments()).append("; ");
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Send notifications after saving (so we have the saved ticket with ID)
        if (newPrimaryAssignee != null) {
            try {
                String resolvedUserId = resolveAssigneeToUserId(newPrimaryAssignee.trim(), savedTicket.getCompanyCode());
                if (resolvedUserId == null) resolvedUserId = newPrimaryAssignee.trim();
                notifyAssigneeOnReassign(savedTicket, resolvedUserId, assignedByUserId, assignedByName);
            } catch (Exception e) {
                System.err.println("Failed to create notification for primary assignee: " + e.getMessage());
            }
        }
        
        if (newSecondaryAssignee != null) {
            try {
                String resolvedUserId = resolveAssigneeToUserId(newSecondaryAssignee.trim(), savedTicket.getCompanyCode());
                if (resolvedUserId == null) resolvedUserId = newSecondaryAssignee.trim();
                notifyAssigneeOnReassign(savedTicket, resolvedUserId, assignedByUserId, assignedByName);
            } catch (Exception e) {
                System.err.println("Failed to create notification for secondary assignee: " + e.getMessage());
            }
        }
        
        if (newThirdAssignee != null) {
            try {
                String resolvedUserId = resolveAssigneeToUserId(newThirdAssignee.trim(), savedTicket.getCompanyCode());
                if (resolvedUserId == null) resolvedUserId = newThirdAssignee.trim();
                notifyAssigneeOnReassign(savedTicket, resolvedUserId, assignedByUserId, assignedByName);
            } catch (Exception e) {
                System.err.println("Failed to create notification for 3rd assignee: " + e.getMessage());
            }
        }
        
        // Send ticket update notification if there are changes
        if (changes.length() > 0) {
            String changeDesc = changes.toString();
            try {
                java.util.Set<String> notifiedUsers = new java.util.HashSet<>();

                // Notify creator
                if (savedTicket.getUserCode() != null && !savedTicket.getUserCode().trim().isEmpty()) {
                    String creator = savedTicket.getUserCode().trim().toLowerCase();
                    if (notifiedUsers.add(creator)) {
                        notificationService.createTicketUpdateNotification(
                                savedTicket,
                                creator,
                                assignedByUserId,
                                assignedByName,
                                changeDesc);
                    }
                }

                // Notify primary assignee
                if (savedTicket.getAssigneeUserCode() != null && !savedTicket.getAssigneeUserCode().trim().isEmpty()) {
                    String assignee1 = savedTicket.getAssigneeUserCode().trim().toLowerCase();
                    if (notifiedUsers.add(assignee1)) {
                        notificationService.createTicketUpdateNotification(
                                savedTicket,
                                assignee1,
                                assignedByUserId,
                                assignedByName,
                                changeDesc);
                    }
                }

                // Notify secondary assignee
                if (savedTicket.getAssignee2UserCode() != null && !savedTicket.getAssignee2UserCode().trim().isEmpty()) {
                    String assignee2 = savedTicket.getAssignee2UserCode().trim().toLowerCase();
                    if (notifiedUsers.add(assignee2)) {
                        notificationService.createTicketUpdateNotification(
                                savedTicket,
                                assignee2,
                                assignedByUserId,
                                assignedByName,
                                changeDesc);
                    }
                }

                // Notify tertiary assignee
                if (savedTicket.getAssignee3UserCode() != null && !savedTicket.getAssignee3UserCode().trim().isEmpty()) {
                    String assignee3 = savedTicket.getAssignee3UserCode().trim().toLowerCase();
                    if (notifiedUsers.add(assignee3)) {
                        notificationService.createTicketUpdateNotification(
                                savedTicket,
                                assignee3,
                                assignedByUserId,
                                assignedByName,
                                changeDesc);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to send ticket update notifications: " + e.getMessage());
            }
        }
        
        // Add history entry for update
        String changeDesc = changes.length() > 0 ? changes.toString() : "Ticket details updated";
        addHistoryEntry(savedTicket, "UPDATED", null, null, changeDesc, username);

        return savedTicket;
    }

    // Update Only Status
    public Ticket updateStatus(Long id, String status) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket Not Found"));

        ticket.setStatus(status);
        return ticketRepository.save(ticket);
    }

    // Change status with history tracking
    public Ticket changeStatus(Long id, String status, String username, String notes) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket Not Found"));

        String oldStatus = ticket.getStatus();
        ticket.setStatus(status);
        
        // Handle specific status changes
        if ("CLOSED".equalsIgnoreCase(status)) {
            if (ticket.getTicketClosedDate() == null) {
                ticket.setTicketClosedDate(LocalDate.now());
                ticket.setTicketClosedName(username);
                ticket.setClosingNotes(notes);
            } else {
                // Second close after reopen
                ticket.setClosedDate2(LocalDate.now());
                ticket.setClosedBy2Name(username);
                ticket.setClosedDetails2(notes);
            }
            ticket.setClosedDate(LocalDate.now());
            ticket.setClosedBy(username);
        } else if ("RESOLVED".equalsIgnoreCase(status)) {
            ticket.setResolvedDate(LocalDate.now());
            ticket.setResolvedBy(username);
            ticket.setResolutionNotes(notes);
        } else if ("REOPENED".equalsIgnoreCase(status)) {
            ticket.setTicketReopenDate(LocalDate.now());
            ticket.setTicketReopenUserName(username);
            ticket.setReopenReason(notes);
            ticket.setReopenedDate(LocalDate.now());
            ticket.setReopenedBy(username);
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Add history entry
        String description = notes != null ? notes : "Status changed from " + oldStatus + " to " + status;
        addHistoryEntry(savedTicket, "STATUS_CHANGE", oldStatus, status, description, username);

        try {
            String changedByUserId = username != null ? username.toLowerCase() : "system";
            java.util.Set<String> notifiedUsers = new java.util.HashSet<>();

            // Notify creator
            if (savedTicket.getUserCode() != null && !savedTicket.getUserCode().trim().isEmpty()) {
                String creator = savedTicket.getUserCode().trim().toLowerCase();
                if (notifiedUsers.add(creator)) {
                    notificationService.createTicketStatusChangeNotification(
                            savedTicket,
                            creator,
                            changedByUserId,
                            username,
                            oldStatus,
                            status);
                }
            }

            // Notify primary assignee
            if (savedTicket.getAssigneeUserCode() != null && !savedTicket.getAssigneeUserCode().trim().isEmpty()) {
                String assignee1 = savedTicket.getAssigneeUserCode().trim().toLowerCase();
                if (notifiedUsers.add(assignee1)) {
                    notificationService.createTicketStatusChangeNotification(
                            savedTicket,
                            assignee1,
                            changedByUserId,
                            username,
                            oldStatus,
                            status);
                }
            }

            // Notify secondary assignee
            if (savedTicket.getAssignee2UserCode() != null && !savedTicket.getAssignee2UserCode().trim().isEmpty()) {
                String assignee2 = savedTicket.getAssignee2UserCode().trim().toLowerCase();
                if (notifiedUsers.add(assignee2)) {
                    notificationService.createTicketStatusChangeNotification(
                            savedTicket,
                            assignee2,
                            changedByUserId,
                            username,
                            oldStatus,
                            status);
                }
            }

            // Notify tertiary assignee
            if (savedTicket.getAssignee3UserCode() != null && !savedTicket.getAssignee3UserCode().trim().isEmpty()) {
                String assignee3 = savedTicket.getAssignee3UserCode().trim().toLowerCase();
                if (notifiedUsers.add(assignee3)) {
                    notificationService.createTicketStatusChangeNotification(
                            savedTicket,
                            assignee3,
                            changedByUserId,
                            username,
                            oldStatus,
                            status);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to send status change notifications: " + e.getMessage());
        }

        return savedTicket;
    }

        // Assign ticket
    public Ticket assignTicket(Long id, String assignedTo, String assignedByUserId, String assignedByName) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket Not Found"));

        // Trim and validate assignedTo
        String originalInput = assignedTo != null ? assignedTo.trim() : null;
        if (originalInput == null || originalInput.isEmpty()) {
            throw new RuntimeException("Assignee cannot be empty");
        }
        
        // Resolve assignee name/email to userId before saving
        String resolvedUserId = resolveAssigneeToUserId(originalInput, ticket.getCompanyCode());

        String oldAssignee = ticket.getAssignedTo();
        ticket.setAssignedTo(resolvedUserId);
        ticket.setAssignedDate(LocalDate.now());
        
        String assigneeLevel = null;
        // Also set to assignee user code based on which slot is available
        if (ticket.getAssigneeUserCode() == null) {
            ticket.setAssigneeUserCode(resolvedUserId);
            assigneeLevel = "Primary Assignee";
        } else if (ticket.getAssignee2UserCode() == null) {
            ticket.setAssignee2UserCode(resolvedUserId);
            assigneeLevel = "Secondary Assignee";
        } else {
            ticket.setAssignee3UserCode(resolvedUserId);
            assigneeLevel = "3rd Assignee";
        }
        
        // Update status to ASSIGNED if it was OPEN
        if ("OPEN".equalsIgnoreCase(ticket.getStatus())) {
            ticket.setStatus("ASSIGNED");
        }
        
        // Update assignee name
        updateAssigneeName(ticket);

        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Add history entry (use original input for display)
        String description = "Ticket assigned to " + originalInput + " as " + assigneeLevel;
        addHistoryEntry(savedTicket, "ASSIGNED", oldAssignee, originalInput, description, assignedByName);

        // Create notification for the assignee with appropriate level
        try {
            String normalizedAssignedBy = assignedByUserId != null ? assignedByUserId.trim().toLowerCase() : "system";
            // Crucial: Use the RESOLVED userId (email) for the notification, not the input name/string
            String normalizedAssignedTo = resolvedUserId != null ? resolvedUserId.toLowerCase() : originalInput.toLowerCase();
            
            System.out.println("=== CREATING NOTIFICATION FOR " + assigneeLevel + " ===");
            System.out.println("Ticket ID: " + savedTicket.getId());
            System.out.println("Ticket No: " + savedTicket.getTicketNo());
            System.out.println("Assignee Input: '" + originalInput + "'");
            System.out.println("Resolved UserId: '" + normalizedAssignedTo + "'");
            System.out.println("Level: " + assigneeLevel);
            System.out.println("Sender: '" + normalizedAssignedBy + "'");
            
            Notification notification = notifyAssigneeOnReassign(
                savedTicket,
                normalizedAssignedTo,
                normalizedAssignedBy,
                assignedByName != null ? assignedByName : "System"
            );
            
            // Also notify creator about assignment
            if (savedTicket.getUserCode() != null && !savedTicket.getUserCode().trim().isEmpty()) {
                String creator = savedTicket.getUserCode().trim().toLowerCase();
                if (!creator.equalsIgnoreCase(normalizedAssignedTo)) {
                    notificationService.createTicketAssignmentNotification(
                        savedTicket,
                        creator,
                        normalizedAssignedBy,
                        assignedByName != null ? assignedByName : "System"
                    );
                }
            }
            
            System.out.println("=== NOTIFICATION CREATED SUCCESSFULLY ===");
            System.out.println("Notification ID: " + notification.getId());
            System.out.println("==========================================");
        } catch (Exception e) {
            // Log but don't fail the assignment
            System.err.println("=== FAILED TO CREATE NOTIFICATION ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

        return savedTicket;
    }
    
    /** Mandatory email when a ticket is first created. */
    private Notification notifyAssigneeOnTicketCreated(
            Ticket ticket,
            String assigneeUserId,
            String createdByUserId,
            String createdByName,
            String assigneeLevel) {
        return notificationService.notifyAssigneeOnTicketCreated(
                ticket, assigneeUserId, createdByUserId, createdByName, assigneeLevel);
    }

    /** In-app only when assignee changes on update/reassign (no creation email). */
    private Notification notifyAssigneeOnReassign(
            Ticket ticket,
            String assigneeUserId,
            String assignedByUserId,
            String assignedByName) {
        String resolved = resolveAssigneeToUserId(assigneeUserId.trim(), ticket.getCompanyCode());
        if (resolved == null) {
            resolved = assigneeUserId.trim().toLowerCase();
        }
        return notificationService.createTicketAssignmentNotification(
                ticket, resolved, assignedByUserId, assignedByName);
    }
    
    /**
     * Resolve assignee input (could be userId/email or name) to actual userId (email)
     * This handles cases where users enter names like "ramya reddy" instead of email
     */
    private String resolveAssigneeToUserId(String assigneeInput, String companyCode) {
        System.out.println("--- resolveAssigneeToUserId called ---");
        System.out.println("Input: '" + assigneeInput + "', Company: " + companyCode);

        if (assigneeInput == null || assigneeInput.trim().isEmpty()) {
            System.out.println("Empty input, returning null.");
            return null;
        }

        String trimmed = assigneeInput.trim();
        System.out.println("Trimmed input: '" + trimmed + "'");

        // 0. If input looks like a valid email, return it directly (trimmed & lowercase)
        if (trimmed.contains("@") && trimmed.contains(".")) {
            System.out.println("Input is an email address, returning directly: " + trimmed);
            return trimmed.toLowerCase();
        }

        // 1. Try finding by userId (exact or case-insensitive)
        UserAccount user = userAccountRepository.findByUserId(trimmed.toLowerCase());
        if (user != null) {
            System.out.println("Found user by userId: " + user.getUserId());
            return user.getUserId().toLowerCase();
        }

        // 2. Try finding by email (exact or case-insensitive)
        Optional<UserAccount> userByEmail = userAccountRepository.findByEmail(trimmed.toLowerCase());
        if (userByEmail.isPresent()) {
            System.out.println("Found user by email: " + userByEmail.get().getUserId());
            return userByEmail.get().getUserId().toLowerCase();
        }

        // 3. Try finding by full name
        Optional<UserAccount> userByName = userAccountRepository.findByFullName(trimmed);
        if (userByName.isPresent()) {
            System.out.println("Found user by full name: " + userByName.get().getUserId());
            return userByName.get().getUserId().toLowerCase();
        }

        // 4. Try splitting name for separate first/last name lookup
        String[] nameParts = trimmed.split("\\s+");
        if (nameParts.length >= 2) {
            String firstName = nameParts[0];
            String lastName = nameParts[nameParts.length - 1]; 
            Optional<UserAccount> userByParts = userAccountRepository.findByFirstNameAndLastName(firstName, lastName);
            if (userByParts.isPresent()) {
                System.out.println("Found user by split first/last name: " + userByParts.get().getUserId());
                return userByParts.get().getUserId().toLowerCase();
            }
        }

        // 5. If company code exists, search in company users
        if (companyCode != null && !companyCode.trim().isEmpty()) {
            List<UserAccount> companyUsers = userAccountRepository.findByCompanyCode(companyCode);
            for (UserAccount u : companyUsers) {
                String fullName = (u.getFirstName() != null ? u.getFirstName() : "") + " " + 
                                 (u.getLastName() != null ? u.getLastName() : "");
                if (fullName.trim().equalsIgnoreCase(trimmed)) {
                    System.out.println("Found user in company by full name match: " + u.getUserId());
                    return u.getUserId().toLowerCase();
                }
            }
            
            // Check company users by first name, last name, partial email, or partial userId
            for (UserAccount u : companyUsers) {
                if (u.getFirstName() != null && u.getFirstName().trim().equalsIgnoreCase(trimmed)) {
                    System.out.println("Found user by firstName match in company: " + u.getUserId());
                    return u.getUserId().toLowerCase();
                }
                if (u.getLastName() != null && u.getLastName().trim().equalsIgnoreCase(trimmed)) {
                    System.out.println("Found user by lastName match in company: " + u.getUserId());
                    return u.getUserId().toLowerCase();
                }
                if (u.getUserId() != null && u.getUserId().toLowerCase().contains(trimmed.toLowerCase())) {
                    System.out.println("Found user by userId partial match in company: " + u.getUserId());
                    return u.getUserId().toLowerCase();
                }
                if (u.getEmail() != null && u.getEmail().toLowerCase().contains(trimmed.toLowerCase())) {
                    System.out.println("Found user by email partial match in company: " + u.getUserId());
                    return u.getUserId().toLowerCase();
                }
            }
        }

        // 6. System-wide fallback for first name, last name, or partial matches using optimized query
        List<UserAccount> fallbackUsers = userAccountRepository.findSystemFallbackUsers(trimmed);
        if (!fallbackUsers.isEmpty()) {
            UserAccount u = fallbackUsers.get(0);
            System.out.println("Found user by fallback query in system: " + u.getUserId());
            return u.getUserId().toLowerCase();
        }

        System.out.println("No user found for input: '" + trimmed + "'");
        return trimmed.toLowerCase(); // Return input as fallback to prevent exceptions and allow email sending
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

    // Add comment
    public TicketComment addComment(Long ticketId, String comment, String username, String attachmentPath) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket Not Found"));

        TicketComment ticketComment = new TicketComment();
        ticketComment.setTicket(ticket);
        ticketComment.setComment(comment);
        ticketComment.setCommentedBy(username);
        ticketComment.setCommentedAt(LocalDateTime.now());
        if (attachmentPath != null && !attachmentPath.isEmpty()) {
            ticketComment.setAttachmentPath(attachmentPath);
        }

        TicketComment savedComment = ticketCommentRepository.save(ticketComment);
        
        // Add history entry
        addHistoryEntry(ticket, "COMMENT_ADDED", null, comment.substring(0, Math.min(50, comment.length())), "Comment added", username);

        // Notify relevant users about the new comment
        try {
            String commentedByUserId = username != null ? username.toLowerCase() : "system";
            String commentPreview = comment.length() > 50 ? comment.substring(0, 47) + "..." : comment;
            
            // 1. Notify the reporter
            if (ticket.getUserCode() != null && !ticket.getUserCode().equalsIgnoreCase(commentedByUserId)) {
                notificationService.createTicketCommentNotification(
                    ticket, 
                    ticket.getUserCode().toLowerCase(), 
                    commentedByUserId, 
                    username, 
                    commentPreview
                );
            }
            
            // 2. Notify the primary assignee
            if (ticket.getAssigneeUserCode() != null && !ticket.getAssigneeUserCode().equalsIgnoreCase(commentedByUserId)) {
                notificationService.createTicketCommentNotification(
                    ticket, 
                    ticket.getAssigneeUserCode().toLowerCase(), 
                    commentedByUserId, 
                    username, 
                    commentPreview
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to send comment notifications: " + e.getMessage());
        }

        return savedComment;
    }

    // Get comments for a ticket
    public List<TicketComment> getComments(Long ticketId) {
        return ticketCommentRepository.findByTicketIdOrderByCommentedAtDesc(ticketId);
    }

    // Get history for a ticket
    public List<TicketHistory> getHistory(Long ticketId) {
        return ticketHistoryRepository.findByTicketIdOrderByChangedAtDesc(ticketId);
    }

    // Add history entry helper
    private void addHistoryEntry(Ticket ticket, String action, String oldValue, String newValue, String description, String changedBy) {
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setAction(action);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setDescription(description);
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());
        ticketHistoryRepository.save(history);
    }

    // Fetch all tickets
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedDateDesc();
    }

    // Fetch tickets assigned to the current (logged-in) user across all assignee slots
    public List<Ticket> getMyAssignedTickets(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return ticketRepository.findMyAssignedTickets(userId.trim().toLowerCase());
    }

    // Fetch tickets assigned to the current user, filtered by company
    public List<Ticket> getMyAssignedTicketsByCompany(String userId, String companyCode) {
        if (userId == null || userId.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        if (companyCode == null || companyCode.trim().isEmpty()) {
            return getMyAssignedTickets(userId);
        }
        return ticketRepository.findMyAssignedTicketsByCompany(
            userId.trim().toLowerCase(), companyCode.trim());
    }

    // Fetch tickets by status
    public List<Ticket> getByStatus(String status) {
        return ticketRepository.findByStatus(status);
    }

    // Fetch tickets by company code
    public List<Ticket> getByCompanyCode(String companyCode) {
        return ticketRepository.findByCompanyCodeOrderByCreatedDateDesc(companyCode);
    }
    
    // Fetch tickets by company and department
    public List<Ticket> getTicketsByCompanyAndDepartment(String companyCode, String departmentCode) {
        if (departmentCode == null || departmentCode.isEmpty()) {
            return getByCompanyCode(companyCode);
        }
        return ticketRepository.findByCompanyCodeAndDepartmentCodeOrderByCreatedDateDesc(companyCode, departmentCode);
    }

    // Fetch tickets by issue type and company
    public List<Ticket> getTicketsByIssueType(String companyCode, String issueType) {
        if (companyCode != null && !companyCode.isEmpty()) {
            return ticketRepository.findByCompanyCodeAndIssueType(companyCode, issueType);
        }
        return ticketRepository.findByIssueType(issueType);
    }
    
    // Fetch tickets by issue type, company, and department
    public List<Ticket> getTicketsByIssueTypeAndDepartment(String companyCode, String departmentCode, String issueType) {
        if (departmentCode == null || departmentCode.isEmpty()) {
            return getTicketsByIssueType(companyCode, issueType);
        }
        return ticketRepository.findByCompanyCodeAndDepartmentCodeAndIssueType(companyCode, departmentCode, issueType);
    }

    // Fetch single ticket
    public Ticket getById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket Not Found"));
    }

    // Soft delete ticket (moves to deleted tickets)
    public void deleteTicket(Long id, String username) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket Not Found"));
        
        ticket.setDeleted(true);
        ticket.setDeletedDate(LocalDateTime.now());
        ticket.setDeletedBy(username);
        
        ticketRepository.save(ticket);
        
        // Add history entry
        addHistoryEntry(ticket, "DELETED", null, null, "Ticket moved to deleted tickets", username);
    }
    
    // Delete ticket (overloaded for backward compatibility)
    public void deleteTicket(Long id) {
        deleteTicket(id, "System");
    }
    
    // Restore deleted ticket
    public Ticket restoreTicket(Long id, String username) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket Not Found"));
        
        ticket.setDeleted(false);
        ticket.setDeletedDate(null);
        ticket.setDeletedBy(null);
        
        Ticket restoredTicket = ticketRepository.save(ticket);
        
        // Add history entry
        addHistoryEntry(restoredTicket, "RESTORED", null, null, "Ticket restored from deleted tickets", username);
        
        return restoredTicket;
    }
    
    // Permanently delete ticket (Admin only)
    public void permanentlyDeleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }
    
    // Get all deleted tickets
    public List<Ticket> getDeletedTickets() {
        return ticketRepository.findAllDeleted();
    }
    
    // Get deleted tickets by company code
    public List<Ticket> getDeletedTicketsByCompanyCode(String companyCode) {
        return ticketRepository.findDeletedByCompanyCode(companyCode);
    }
    
    // Count deleted tickets
    public long countDeletedTickets() {
        return ticketRepository.countDeleted();
    }
    
    // Count deleted tickets by company
    public long countDeletedTicketsByCompanyCode(String companyCode) {
        return ticketRepository.countDeletedByCompanyCode(companyCode);
    }

    /**
     * Helper to update the primary assignee name and keep assignedTo in sync.
     */
    private void updateAssigneeName(Ticket ticket) {
        String userCode = ticket.getAssigneeUserCode();
        if (userCode != null && !userCode.trim().isEmpty()) {
            UserAccount user = userAccountRepository.findByUserId(userCode.trim().toLowerCase());
            if (user != null) {
                String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + " " + 
                                 (user.getLastName() != null ? user.getLastName() : "");
                ticket.setAssigneeName(fullName.trim());
                // For backward compatibility and list views, ensure assignedTo is also set to the userId (email)
                ticket.setAssignedTo(user.getUserId());
            } else {
                // Fallback if user not found in DB
                ticket.setAssigneeName(userCode);
                ticket.setAssignedTo(userCode);
            }
        } else if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().trim().isEmpty()) {
            // If assignedTo is set but assigneeUserCode is not, try to sync
            String legacyAssignee = ticket.getAssignedTo().trim();
            UserAccount user = userAccountRepository.findByUserId(legacyAssignee.toLowerCase());
            if (user != null) {
                String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + " " + 
                                 (user.getLastName() != null ? user.getLastName() : "");
                ticket.setAssigneeName(fullName.trim());
                ticket.setAssigneeUserCode(user.getUserId());
            } else {
                ticket.setAssigneeName(legacyAssignee);
            }
        } else {
            ticket.setAssigneeName(null);
            ticket.setAssignedTo(null);
        }
    }

    public Ticket save(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public Optional<Ticket> findById(Long id) {
        return ticketRepository.findById(id);
    }

    // Add attachments to ticket
    public Ticket addAttachments(Long ticketId, String newAttachments, String username) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket Not Found"));
        
        String oldAttachments = ticket.getAttachments() != null ? ticket.getAttachments() : ticket.getAttachmentPaths();
        String merged;
        if (oldAttachments == null || oldAttachments.trim().isEmpty()) {
            merged = newAttachments.trim();
        } else {
            merged = oldAttachments.trim() + "," + newAttachments.trim();
        }
        ticket.setAttachments(merged);
        ticket.setAttachmentPaths(merged);
        
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Add history entry
        addHistoryEntry(ticket, "ATTACHMENT_ADDED", null, newAttachments, "New attachment(s) uploaded", username);
        
        return savedTicket;
    }

    // Delete an attachment from a ticket
    public Ticket deleteAttachment(Long ticketId, String fileName, String username) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket Not Found"));

        String currentAttachments = ticket.getAttachments() != null ? ticket.getAttachments() : ticket.getAttachmentPaths();
        if (currentAttachments != null && !currentAttachments.trim().isEmpty()) {
            String[] split = currentAttachments.split(",");
            StringBuilder remaining = new StringBuilder();
            for (String f : split) {
                String trimmed = f.trim();
                if (!trimmed.isEmpty() && !trimmed.equalsIgnoreCase(fileName.trim())) {
                    if (remaining.length() > 0) remaining.append(",");
                    remaining.append(trimmed);
                }
            }
            String updatedStr = remaining.toString();
            ticket.setAttachments(updatedStr);
            ticket.setAttachmentPaths(updatedStr);
            Ticket savedTicket = ticketRepository.save(ticket);

            // Delete file from disk if present
            try {
                java.io.File file = java.nio.file.Paths.get("uploads", "tickets", fileName.trim()).toFile();
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                // Log warning if file deletion fails
            }

            addHistoryEntry(ticket, "ATTACHMENT_DELETED", fileName, null, "Attachment deleted: " + fileName, username);
            return savedTicket;
        }
        return ticket;
    }

    // Replace an attachment on a ticket
    public Ticket replaceAttachment(Long ticketId, String oldFileName, String newFileName, String username) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket Not Found"));

        String currentAttachments = ticket.getAttachments() != null ? ticket.getAttachments() : ticket.getAttachmentPaths();
        StringBuilder updated = new StringBuilder();
        boolean replaced = false;

        if (currentAttachments != null && !currentAttachments.trim().isEmpty()) {
            String[] split = currentAttachments.split(",");
            for (String f : split) {
                String trimmed = f.trim();
                if (trimmed.isEmpty()) continue;
                if (updated.length() > 0) updated.append(",");
                if (trimmed.equalsIgnoreCase(oldFileName.trim())) {
                    updated.append(newFileName.trim());
                    replaced = true;
                } else {
                    updated.append(trimmed);
                }
            }
        }
        if (!replaced) {
            if (updated.length() > 0) updated.append(",");
            updated.append(newFileName.trim());
        }

        String updatedStr = updated.toString();
        ticket.setAttachments(updatedStr);
        ticket.setAttachmentPaths(updatedStr);
        Ticket savedTicket = ticketRepository.save(ticket);

        // Delete old file from disk if present
        try {
            java.io.File file = java.nio.file.Paths.get("uploads", "tickets", oldFileName.trim()).toFile();
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            // Log warning if old file deletion fails
        }

        addHistoryEntry(ticket, "ATTACHMENT_REPLACED", oldFileName, newFileName, "Attachment replaced: " + oldFileName + " -> " + newFileName, username);
        return savedTicket;
    }
}
