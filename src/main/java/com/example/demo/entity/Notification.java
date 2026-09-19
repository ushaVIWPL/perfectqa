package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who should receive this notification
    @Column(name = "recipient_user_id", nullable = false, length = 255)
    private String recipientUserId;

    // Who triggered this notification
    @Column(name = "sender_user_id", length = 255)
    private String senderUserId;
    
    @Column(name = "sender_name", length = 255)
    private String senderName;

    // Notification details
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    // Type of notification: TICKET_ASSIGNED, TICKET_STATUS_CHANGED, TICKET_COMMENT, etc.
    @Column(nullable = false)
    private String notificationType;

    @Column(name = "type")
    private String type;

    // Reference to related entity (e.g., ticket ID)
    private Long referenceId;
    private String referenceType; // TICKET, TASK, etc.
    private String referenceCode; // Ticket number, etc.

    // Link to navigate when clicked
    private String actionUrl;

    // Status
    @Column(nullable = false)
    private Boolean isRead = false;

    private LocalDateTime readAt;

    // Timestamps
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Company context
    private String companyCode;

    // Priority for sorting (higher = more important)
    private Integer priority = 0;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isRead == null) {
            this.isRead = false;
        }
        if (this.type == null) {
            this.type = this.notificationType;
        }
        // CRITICAL: Ensure recipientUserId is never null or empty
        if (this.recipientUserId == null || this.recipientUserId.trim().isEmpty()) {
            throw new IllegalStateException("Cannot persist Notification: recipientUserId cannot be null or empty. Current value: '" + this.recipientUserId + "'");
        }
        this.recipientUserId = this.recipientUserId.trim().toLowerCase();
    }

    @PreUpdate
    public void preUpdate() {
        if (this.recipientUserId != null) {
            this.recipientUserId = this.recipientUserId.trim().toLowerCase();
        }
    }

    // Explicit Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(String recipientUserId) { this.recipientUserId = recipientUserId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { 
        this.notificationType = notificationType; 
        this.type = notificationType;
    }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }
}





