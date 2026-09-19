package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "support_requests")
public class SupportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "status", length = 50)
    private String status = "PENDING"; // PENDING, CONTACTED, RESOLVED, CLOSED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "contacted_at")
    private LocalDateTime contactedAt;

    @Column(name = "contacted_by", length = 255)
    private String contactedBy; // Admin who contacted the customer

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "PENDING";
        }
    }
}

